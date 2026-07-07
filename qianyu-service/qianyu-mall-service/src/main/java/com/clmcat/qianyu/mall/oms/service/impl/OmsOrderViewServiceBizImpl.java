package com.clmcat.qianyu.mall.oms.service.impl;

import com.clmcat.qianyu.mall.oms.rpc.OmsOrderApiImpl;
import com.clmcat.qianyu.mall.api.ads.AdsAddressApi;
import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;
import com.clmcat.qianyu.mall.api.pms.PmsSkuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSkuDto;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsCreateDTO;
import com.clmcat.qianyu.mall.log.service.LogisticsViewServiceBiz;
import com.clmcat.qianyu.mall.oms.mapper.OmsOrderMapper;
import com.clmcat.qianyu.mall.oms.model.dto.*;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrder;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrderItem;
import com.clmcat.qianyu.mall.oms.model.entity.status.OmsStatus;
import com.clmcat.qianyu.mall.oms.model.vo.*;
import com.clmcat.qianyu.mall.oms.support.OmsSupport;
import com.clmcat.qianyu.mall.pay.mapper.PayPaymentMapper;
import com.clmcat.qianyu.mall.pay.model.entity.PayPayment;
import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.PpcUserInfoListDto;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.clmcat.qianyu.mall.oms.service.OmsOrderViewServiceBiz;

@Service
public class OmsOrderViewServiceBizImpl implements OmsOrderViewServiceBiz {

    @Resource
    private OmsOrderApiImpl orderServiceBiz;

    @Resource
    private OmsOrderMapper orderMapper;

    @Resource
    private PayPaymentMapper paymentMapper;

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @DubboReference
    private PmsSkuApi pmsSkuApi;

    @DubboReference
    private InvStockApi invStockApi;

    @DubboReference
    private AdsAddressApi adsAddressApi;

    @Resource
    private org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;

    @DubboReference
    private UserApi userApi;

    @Resource
    private LogisticsViewServiceBiz logisticsViewServiceBiz;

    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(Long userId, OrderCreateDTO dto) {
        OmsStatus.OMS_ADDRESS_REQUIRED.assertThrowResEx(dto.getAddressId() == null);
        OmsStatus.OMS_ORDER_ITEMS_EMPTY.assertThrowResEx(dto.getItems() == null || dto.getItems().isEmpty());

        // P0-2: address existence + ownership validation (prevent info leak via addressSnapshot)
        com.clmcat.qianyu.mall.api.ads.model.dto.AdsAddressDto validatedAddress = adsAddressApi.getById(dto.getAddressId());
        OmsStatus.OMS_ADDRESS_NOT_FOUND.assertThrowResEx(validatedAddress == null);
        OmsStatus.OMS_ADDRESS_NOT_BELONG_USER.assertThrowResEx(!userId.equals(validatedAddress.getUserId()));

        // P0-1: clientToken 幂等锁（防双击重复下单，锁 30s 自动过期）
        if (dto.getClientToken() != null && !dto.getClientToken().isEmpty()) {
            com.clmcat.qianyu.core.redis.RedisLock idemLock =
                    com.clmcat.qianyu.core.redis.RedisLockSupport.newLock(redisTemplate, 30);
            OmsStatus.OMS_ORDER_DUPLICATE_REQUEST.assertThrowResEx(
                    !idemLock.lock("order:create:" + userId + ":" + dto.getClientToken(), 2000));
        }

        long now = System.currentTimeMillis();
        Long orderId = OmsSupport.ORDER_ID_SNOWFLAKE.nextId();
        String orderNo = "ORD" + orderId;

        // Build order items with real SKU lookup
        List<OmsOrderItem> orderItems = new ArrayList<>();
        int totalQuantity = 0;
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        Long merchantId = 0L;

        for (OrderItemDTO itemDTO : dto.getItems()) {
            Long itemId = OmsSupport.ORDER_ITEM_ID_SNOWFLAKE.nextId();

            // Lookup SKU info via RPC
            PmsSkuDto skuDto = pmsSkuApi.getById(itemDTO.getSkuId());
            OmsStatus.OMS_SKU_NOT_FOUND.assertThrowResEx(skuDto == null);
            java.math.BigDecimal price = java.math.BigDecimal.ONE;
            Long spuId = 0L;
            String skuName = "SKU-" + itemDTO.getSkuId();
            String skuImage = "";
            String attributes = "[]";

            if (skuDto != null) {
                price = skuDto.getPrice() != null ? skuDto.getPrice() : java.math.BigDecimal.ONE;
                spuId = skuDto.getSpuId() != null ? skuDto.getSpuId() : 0L;
                skuName = skuDto.getSkuName() != null ? skuDto.getSkuName() : "SKU-" + itemDTO.getSkuId();
                skuImage = skuDto.getSkuImage() != null ? skuDto.getSkuImage() : "";
                // attributes is a MySQL JSON column — must be valid JSON.
                // skuDto.getAttributes() is List<LinkedHashMap<String,String>>, .toString() yields Java format.
                if (skuDto.getAttributes() != null && !skuDto.getAttributes().isEmpty()) {
                    try {
                        attributes = JSON_MAPPER.writeValueAsString(skuDto.getAttributes());
                    } catch (Exception e) {
                        attributes = "[]";
                    }
                }
                merchantId = skuDto.getMerchantId() != null ? skuDto.getMerchantId() : 0L;
            }

            java.math.BigDecimal itemTotal = price.multiply(java.math.BigDecimal.valueOf(itemDTO.getQuantity()));

            OmsOrderItem item = new OmsOrderItem();
            item.setId(itemId);
            item.setOrderId(orderId);
            item.setMerchantId(merchantId);
            item.setSpuId(spuId);
            item.setSkuId(itemDTO.getSkuId());
            item.setSkuName(skuName);
            item.setSkuImage(skuImage);
            item.setPrice(price);
            item.setQuantity(itemDTO.getQuantity());
            item.setTotalAmount(itemTotal);
            item.setAttributes(attributes);
            item.setCreateTime(now);
            item.setUpdateTime(now);
            orderItems.add(item);

            totalQuantity += itemDTO.getQuantity();
            totalAmount = totalAmount.add(itemTotal);
        }

        // Lock stock via INV module
        List<InvStockDto.StockLockItem> lockItems = new ArrayList<>();
        for (OrderItemDTO itemDTO : dto.getItems()) {
            InvStockDto.StockLockItem lockItem = new InvStockDto.StockLockItem();
            lockItem.setSkuId(itemDTO.getSkuId());
            lockItem.setQuantity(itemDTO.getQuantity());
            lockItems.add(lockItem);
        }
        // P0-1: stock lock failure is fatal — order must not be created without stock lock
        invStockApi.lockStock(orderNo, lockItems);
        // P0-1: declare before try (used in return outside try block)
        java.math.BigDecimal freightAmount = java.math.BigDecimal.ZERO;
        java.math.BigDecimal payAmount = totalAmount.add(freightAmount);
        // P0-1: compensating release on failure (INV is remote, not in local tx)
        try {
        // --- order build + insert (if this fails, release stock) ---

        OmsOrder order = new OmsOrder();
        order.setId(orderId);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setFreightAmount(freightAmount);
        order.setCouponAmount(java.math.BigDecimal.ZERO);
        order.setDiscountAmount(java.math.BigDecimal.ZERO);
        order.setTotalQuantity(totalQuantity);
        order.setStatus(OmsOrder.STATUS_PENDING_PAY);
        order.setAfterSaleStatus(0);
        order.setAfterSaleType(0);
        order.setVersion(0L);
        order.setSource(1);
        order.setBuyerMessage(dto.getRemark());
        order.setBuyerIp(dto.getBuyerIp());
        order.setCouponUserId(dto.getCouponUserId());

        // P0-2: snapshot from validated address (no re-fetch, no swallow)
        String addressSnapshot = null;
        try {
            addressSnapshot = JSON_MAPPER.writeValueAsString(validatedAddress);
        } catch (Exception e) {
            // JSON serialization failure — non-fatal
        }
        order.setAddressSnapshot(addressSnapshot);

        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setDeleted(0);

        orderServiceBiz.insertOrderWithItems(order, orderItems);
        } catch (Exception e) {
            // P0-1: 补偿释放——insert 失败须释放已锁库存（INV 是 remote，不在本地 tx）
            try { invStockApi.releaseStock(orderNo, lockItems); } catch (Exception ex) { /* best-effort */ }
            throw e;
        }

        return OrderCreateVO.builder()
                .orderId(orderId)
                .orderSn(orderNo)
                .totalAmount(totalAmount.toPlainString())
                .freightAmount(freightAmount.toPlainString())
                .discountAmount(java.math.BigDecimal.ZERO.toPlainString())
                .couponAmount(java.math.BigDecimal.ZERO.toPlainString())
                .payAmount(payAmount.toPlainString())
                .totalQuantity(totalQuantity)
                .build();
    }

    public Page<OrderSimpleVO> orderList(Long userId, OrderQueryDTO dto) {
        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;
        Integer status = dto != null ? dto.getStatus() : null;

        // Map DTO status (0-6) to DB query conditions
        QueryWrapper qw = QueryWrapper.create()
                .where("user_id = ?", userId).and("deleted = 0");
        if (status != null && status != 0) {
            switch (status) {
                case 1 -> qw.and("status = 10");
                case 2 -> qw.and("status = 20");
                case 3 -> qw.and("status = 30");
                case 4 -> qw.and("status = 40");
                case 5 -> qw.and("after_sale_status > 0");
                case 6 -> qw.and("status IN (50, 60)");
                default -> { /* no additional filter */ }
            }
        }
        qw.orderBy("create_time DESC");

        Page<OmsOrder> orderPage = orderMapper.paginate(new Page<>(pageNum, pageSize), qw);
        if (orderPage == null || orderPage.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        List<OrderSimpleVO> voList = new ArrayList<>();
        for (OmsOrder order : orderPage.getRecords()) {
            List<OmsOrderItem> items = orderServiceBiz.findItemsByOrderId(order.getId());
            List<OrderItemSimpleVO> itemVos = new ArrayList<>();
            for (OmsOrderItem item : items) {
                itemVos.add(OrderItemSimpleVO.builder()
                        .skuImage(item.getSkuImage())
                        .spuName(item.getSkuName())
                        .skuSpecs(item.getAttributes())
                        .price(item.getPrice() != null ? item.getPrice().toPlainString() : "0.00")
                        .quantity(item.getQuantity())
                        .build());
            }

            voList.add(OrderSimpleVO.builder()
                    .orderId(order.getId())
                    .orderSn(order.getOrderNo())
                    .status(order.getStatus())
                    .afterSaleStatus(order.getAfterSaleStatus())
                    .afterSaleType(order.getAfterSaleType())
                    .displayStatus(mapStatusToDisplay(order.getStatus()))
                    .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().toPlainString() : "0.00")
                    .payAmount(order.getPayAmount() != null ? order.getPayAmount().toPlainString() : "0.00")
                    .totalQuantity(order.getTotalQuantity())
                    .items(itemVos)
                    .createTime(formatTime(order.getCreateTime()))
                    .build());
        }

        Page<OrderSimpleVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(orderPage.getTotalRow());
        return result;
    }

    public OrderDetailVO orderDetail(Long userId, Long orderId) {
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(orderId == null);
        OmsOrder order = orderServiceBiz.getOrderById(orderId);
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(order == null);
        OmsStatus.OMS_ORDER_NOT_BELONG_USER.assertThrowResEx(!order.getUserId().equals(userId));

        List<OmsOrderItem> items = orderServiceBiz.findItemsByOrderId(orderId);
        List<OrderItemDetailVO> itemVos = new ArrayList<>();
        for (OmsOrderItem item : items) {
            itemVos.add(OrderItemDetailVO.builder()
                    .id(item.getId())
                    .skuId(item.getSkuId())
                    .spuId(item.getSpuId())
                    .spuName(item.getSkuName())
                    .skuImage(item.getSkuImage())
                    .skuSpecs(item.getAttributes())
                    .price(item.getPrice() != null ? item.getPrice().toPlainString() : "0.00")
                    .quantity(item.getQuantity())
                    .subtotal(item.getTotalAmount() != null ? item.getTotalAmount().toPlainString() : "0.00")
                    .build());
        }

        return OrderDetailVO.builder()
                .orderId(order.getId())
                .orderSn(order.getOrderNo())
                .status(order.getStatus())
                .afterSaleStatus(order.getAfterSaleStatus())
                .afterSaleType(order.getAfterSaleType())
                .displayStatus(mapStatusToDisplay(order.getStatus()))
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().toPlainString() : "0.00")
                .payAmount(order.getPayAmount() != null ? order.getPayAmount().toPlainString() : "0.00")
                .freightAmount(order.getFreightAmount() != null ? order.getFreightAmount().toPlainString() : "0.00")
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount().toPlainString() : "0.00")
                .couponAmount(order.getCouponAmount() != null ? order.getCouponAmount().toPlainString() : "0.00")
                .discountDetail(buildDiscountDetail(order.getDiscountDetail()))
                .remark(order.getBuyerMessage())
                .merchantRemark(order.getMerchantRemark())
                .items(itemVos)
                .address(buildAddressSnapshot(order.getAddressSnapshot()))
                .payment(buildPaymentInfo(order.getId()))
                .source(order.getSource())
                .createTime(formatTime(order.getCreateTime()))
                .payTime(formatTime(order.getPayTime()))
                .shipTime(formatTime(order.getDeliveryTime()))
                .receiveTime(formatTime(order.getReceiveTime()))
                .build();
    }

    public void cancelOrder(Long userId, OrderCancelDTO dto) {
        OmsOrder order = orderServiceBiz.getOrderById(dto.getOrderId());
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(order == null);
        OmsStatus.OMS_ORDER_NOT_BELONG_USER.assertThrowResEx(!order.getUserId().equals(userId));
        OmsStatus.OMS_ORDER_STATUS_ERROR.assertThrowResEx(order.getStatus() != OmsOrder.STATUS_PENDING_PAY);

        order.setStatus(OmsOrder.STATUS_CANCELLED);
        order.setCloseTime(System.currentTimeMillis());
        order.setUpdateTime(System.currentTimeMillis());
        orderServiceBiz.updateOrder(order);

        // Release locked stock via INV module
        List<OmsOrderItem> orderItems = orderServiceBiz.findItemsByOrderId(order.getId());
        if (orderItems != null && !orderItems.isEmpty()) {
            List<InvStockDto.StockLockItem> releaseItems = new ArrayList<>();
            for (OmsOrderItem oi : orderItems) {
                InvStockDto.StockLockItem releaseItem = new InvStockDto.StockLockItem();
                releaseItem.setSkuId(oi.getSkuId());
                releaseItem.setQuantity(oi.getQuantity());
                releaseItems.add(releaseItem);
            }
            try {
                invStockApi.releaseStock(order.getOrderNo(), releaseItems);
            } catch (Exception e) {
                // Release failure should not block cancellation
            }
        }
    }

    public void receiveOrder(Long userId, Long orderId) {
        OmsOrder order = orderServiceBiz.getOrderById(orderId);
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(order == null);
        OmsStatus.OMS_ORDER_NOT_BELONG_USER.assertThrowResEx(!order.getUserId().equals(userId));
        OmsStatus.OMS_ORDER_STATUS_ERROR.assertThrowResEx(order.getStatus() != OmsOrder.STATUS_SHIPPED);

        order.setStatus(OmsOrder.STATUS_COMPLETED);
        order.setReceiveTime(System.currentTimeMillis());
        order.setUpdateTime(System.currentTimeMillis());
        orderServiceBiz.updateOrder(order);
    }

    /**
     * 删除订单（仅已完成/已取消/已关闭）
     */
    public void deleteOrder(Long userId, Long orderId) {
        OmsOrder order = orderServiceBiz.getOrderById(orderId);
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(order == null);
        OmsStatus.OMS_ORDER_NOT_BELONG_USER.assertThrowResEx(!order.getUserId().equals(userId));
        OmsStatus.OMS_ORDER_STATUS_ERROR.assertThrowResEx(
                order.getStatus() != OmsOrder.STATUS_COMPLETED
                && order.getStatus() != OmsOrder.STATUS_CANCELLED
                && order.getStatus() != OmsOrder.STATUS_CLOSED);

        order.setDeleted(1);
        order.setUpdateTime(System.currentTimeMillis());
        orderServiceBiz.updateOrder(order);
    }

    public Page<OrderSimpleVO> merchantOrderList(Long merchantId, OrderQueryDTO dto) {
        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;
        Integer status = dto != null ? dto.getStatus() : null;
        String orderSn = dto != null ? dto.getOrderSn() : null;

        Page<OmsOrder> orderPage = orderServiceBiz.pageByMerchant(merchantId, status, orderSn, pageNum, pageSize);
        if (orderPage == null || orderPage.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        // 批量查询买家昵称（避免逐单 RPC）。降级：RPC 失败时 buyerNick 为空
        List<Long> buyerUserIds = orderPage.getRecords().stream()
                .map(OmsOrder::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, RpcUserInfoDto> userInfoMap = new HashMap<>();
        if (!buyerUserIds.isEmpty()) {
            try {
                PpcUserInfoListDto listDto = userApi.getUserInfoList(buyerUserIds);
                if (listDto != null && listDto.getUsers() != null) {
                    userInfoMap = listDto.getUsers().stream()
                            .filter(u -> u != null && u.getUserId() != null)
                            .collect(Collectors.toMap(RpcUserInfoDto::getUserId, u -> u, (a, b) -> a));
                }
            } catch (Exception e) {
                // 降级：仅 nickname 缺失，列表其他字段正常返回
            }
        }

        List<OrderSimpleVO> voList = new ArrayList<>();
        for (OmsOrder order : orderPage.getRecords()) {
            List<OmsOrderItem> items = orderServiceBiz.findItemsByOrderId(order.getId());
            List<OrderItemSimpleVO> itemVos = new ArrayList<>();
            for (OmsOrderItem item : items) {
                itemVos.add(OrderItemSimpleVO.builder()
                        .skuImage(item.getSkuImage())
                        .spuName(item.getSkuName())
                        .skuSpecs(item.getAttributes())
                        .price(item.getPrice() != null ? item.getPrice().toPlainString() : "0.00")
                        .quantity(item.getQuantity())
                        .build());
            }

            RpcUserInfoDto buyer = order.getUserId() != null ? userInfoMap.get(order.getUserId()) : null;
            String buyerNick = buyer != null ? buyer.getNickname() : null;

            voList.add(OrderSimpleVO.builder()
                    .orderId(order.getId())
                    .orderSn(order.getOrderNo())
                    .status(order.getStatus())
                    .afterSaleStatus(order.getAfterSaleStatus())
                    .afterSaleType(order.getAfterSaleType())
                    .displayStatus(mapStatusToDisplay(order.getStatus()))
                    .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().toPlainString() : "0.00")
                    .payAmount(order.getPayAmount() != null ? order.getPayAmount().toPlainString() : "0.00")
                    .totalQuantity(order.getTotalQuantity())
                    .items(itemVos)
                    .createTime(formatTime(order.getCreateTime()))
                    .buyerNick(buyerNick)
                    .build());
        }

        Page<OrderSimpleVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(orderPage.getTotalRow());
        return result;
    }

    public OrderDetailVO merchantOrderDetail(Long merchantId, Long orderId) {
        OmsOrder order = orderServiceBiz.getOrderById(orderId);
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(order == null);
        OmsStatus.OMS_ORDER_NOT_BELONG_MERCHANT.assertThrowResEx(!order.getMerchantId().equals(merchantId));

        List<OmsOrderItem> items = orderServiceBiz.findItemsByOrderId(orderId);
        List<OrderItemDetailVO> itemVos = new ArrayList<>();
        for (OmsOrderItem item : items) {
            itemVos.add(OrderItemDetailVO.builder()
                    .id(item.getId())
                    .skuId(item.getSkuId())
                    .spuId(item.getSpuId())
                    .spuName(item.getSkuName())
                    .skuImage(item.getSkuImage())
                    .skuSpecs(item.getAttributes())
                    .price(item.getPrice() != null ? item.getPrice().toPlainString() : "0.00")
                    .quantity(item.getQuantity())
                    .subtotal(item.getTotalAmount() != null ? item.getTotalAmount().toPlainString() : "0.00")
                    .build());
        }

        return OrderDetailVO.builder()
                .orderId(order.getId())
                .orderSn(order.getOrderNo())
                .status(order.getStatus())
                .afterSaleStatus(order.getAfterSaleStatus())
                .afterSaleType(order.getAfterSaleType())
                .displayStatus(mapStatusToDisplay(order.getStatus()))
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().toPlainString() : "0.00")
                .payAmount(order.getPayAmount() != null ? order.getPayAmount().toPlainString() : "0.00")
                .freightAmount(order.getFreightAmount() != null ? order.getFreightAmount().toPlainString() : "0.00")
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount().toPlainString() : "0.00")
                .couponAmount(order.getCouponAmount() != null ? order.getCouponAmount().toPlainString() : "0.00")
                .remark(order.getBuyerMessage())
                .merchantRemark(order.getMerchantRemark())
                .items(itemVos)
                .source(order.getSource())
                .createTime(formatTime(order.getCreateTime()))
                .payTime(formatTime(order.getPayTime()))
                .shipTime(formatTime(order.getDeliveryTime()))
                .receiveTime(formatTime(order.getReceiveTime()))
                .build();
    }

    public void shipOrder(Long merchantId, OrderShipDTO dto) {
        OmsOrder order = orderServiceBiz.getOrderById(dto.getOrderId());
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(order == null);
        OmsStatus.OMS_ORDER_NOT_BELONG_MERCHANT.assertThrowResEx(!order.getMerchantId().equals(merchantId));
        OmsStatus.OMS_ORDER_STATUS_ERROR.assertThrowResEx(order.getStatus() != OmsOrder.STATUS_PENDING_SHIP);

        order.setStatus(OmsOrder.STATUS_SHIPPED);
        order.setDeliveryTime(System.currentTimeMillis());
        order.setUpdateTime(System.currentTimeMillis());
        orderServiceBiz.updateOrder(order);

        // 同步创建物流记录，发货后用户才能查物流轨迹
        // 注：userId 在 createLogistics 内部未使用，仅作为身份占位
        LogisticsCreateDTO logisticsDTO = new LogisticsCreateDTO();
        logisticsDTO.setOrderId(order.getId());
        logisticsDTO.setLogisticsCompany(dto.getLogisticsCompany());
        logisticsDTO.setLogisticsNo(dto.getLogisticsNo());
        try {
            logisticsViewServiceBiz.createLogistics(0L, logisticsDTO);
        } catch (Exception e) {
            // 物流记录创建失败不回滚订单状态（主流程已成功）；
            // 极端场景：旧物流记录已存在（拆单/重复发货），由后续重试或运营介入
        }
    }

    /**
     * Internal helper - get order entity by ID
     */
    public OmsOrder getOrderById(Long orderId) {
        return orderServiceBiz.getOrderById(orderId);
    }

    private String mapStatusToDisplay(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 10 -> "待付款";
            case 20 -> "待发货";
            case 30 -> "待收货";
            case 40 -> "已完成";
            case 50 -> "已取消";
            case 60 -> "已关闭";
            default -> "";
        };
    }

    private String formatTime(Long millis) {
        if (millis == null || millis <= 0) return "";
        java.time.LocalDateTime ldt = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(millis), java.time.ZoneId.systemDefault());
        return ldt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private AddressSnapshotVO buildAddressSnapshot(String addressSnapshot) {
        if (addressSnapshot == null || addressSnapshot.isEmpty()) return null;
        try {
            Map<String, Object> map = JSON_MAPPER.readValue(addressSnapshot, new TypeReference<Map<String, Object>>() {});
            return AddressSnapshotVO.builder()
                    .name(map.get("name") != null ? map.get("name").toString() : null)
                    .phone(map.get("phone") != null ? map.get("phone").toString() : null)
                    .province(map.get("province") != null ? map.get("province").toString() : null)
                    .city(map.get("city") != null ? map.get("city").toString() : null)
                    .district(map.get("district") != null ? map.get("district").toString() : null)
                    .detail(map.get("detail") != null ? map.get("detail").toString() : null)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private PaymentInfoVO buildPaymentInfo(Long orderId) {
        try {
            QueryWrapper qw = QueryWrapper.create()
                    .where("order_id = ?", orderId)
                    .and("pay_status = 20")
                    .and("deleted = 0")
                    .limit(1);
            PayPayment payment = paymentMapper.selectOneByQuery(qw);
            if (payment == null) return null;

            String channelName = switch (payment.getPayChannel()) {
                case 1 -> "微信支付";
                case 2 -> "支付宝";
                case 3 -> "余额支付";
                default -> "在线支付";
            };
            return PaymentInfoVO.builder()
                    .paySn(payment.getPaymentNo())
                    .payChannel(channelName)
                    .payTime(formatTime(payment.getPayTime()))
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private List<DiscountItemVO> buildDiscountDetail(String discountDetail) {
        if (discountDetail == null || discountDetail.isEmpty()) return null;
        try {
            List<Map<String, Object>> list = JSON_MAPPER.readValue(discountDetail, new TypeReference<List<Map<String, Object>>>() {});
            List<DiscountItemVO> result = new ArrayList<>();
            for (Map<String, Object> item : list) {
                result.add(DiscountItemVO.builder()
                        .type(item.get("type") != null ? item.get("type").toString() : null)
                        .id(item.get("id") != null ? Long.valueOf(item.get("id").toString()) : null)
                        .name(item.get("name") != null ? item.get("name").toString() : null)
                        .amount(item.get("amount") != null ? item.get("amount").toString() : null)
                        .build());
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
