package com.clmcat.qianyu.mall.oms.service;

import com.clmcat.qianyu.mall.api.ads.AdsAddressApi;
import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;
import com.clmcat.qianyu.mall.api.pms.PmsSkuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSkuDto;
import com.clmcat.qianyu.mall.oms.model.dto.*;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrder;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrderItem;
import com.clmcat.qianyu.mall.oms.model.entity.status.OmsStatus;
import com.clmcat.qianyu.mall.oms.model.vo.*;
import com.clmcat.qianyu.mall.oms.support.OmsSupport;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OmsOrderViewServiceBiz {

    @Resource
    private OmsOrderServiceBiz orderServiceBiz;

    @DubboReference
    private PmsSkuApi pmsSkuApi;

    @DubboReference
    private InvStockApi invStockApi;

    @DubboReference
    private AdsAddressApi adsAddressApi;

    public OrderCreateVO createOrder(Long userId, OrderCreateDTO dto) {
        OmsStatus.OMS_ADDRESS_REQUIRED.assertThrowResEx(dto.getAddressId() == null);
        OmsStatus.OMS_ORDER_ITEMS_EMPTY.assertThrowResEx(dto.getItems() == null || dto.getItems().isEmpty());

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
            String attributes = "";

            if (skuDto != null) {
                price = skuDto.getPrice() != null ? skuDto.getPrice() : java.math.BigDecimal.ONE;
                spuId = skuDto.getSpuId() != null ? skuDto.getSpuId() : 0L;
                skuName = skuDto.getSkuName() != null ? skuDto.getSkuName() : "SKU-" + itemDTO.getSkuId();
                skuImage = skuDto.getSkuImage() != null ? skuDto.getSkuImage() : "";
                attributes = skuDto.getAttributes() != null ? skuDto.getAttributes().toString() : "";
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
        try {
            invStockApi.lockStock(orderNo, lockItems);
        } catch (Exception e) {
            // Stock lock failure is not fatal for order creation in this phase
        }

        java.math.BigDecimal freightAmount = java.math.BigDecimal.ZERO;
        java.math.BigDecimal payAmount = totalAmount.add(freightAmount);

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

        // Fetch address snapshot
        String addressSnapshot = null;
        try {
            com.clmcat.qianyu.mall.api.ads.model.dto.AdsAddressDto addressDto = adsAddressApi.getById(dto.getAddressId());
            if (addressDto != null) {
                addressSnapshot = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(addressDto);
            }
        } catch (Exception e) {
            // Address lookup failure should not block order creation
        }
        order.setAddressSnapshot(addressSnapshot);

        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setDeleted(0);

        orderServiceBiz.insertOrderWithItems(order, orderItems);

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

        Page<OmsOrder> orderPage = orderServiceBiz.pageByUser(userId, status, pageNum, pageSize);
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

    public Page<OrderSimpleVO> merchantOrderList(Long merchantId, OrderQueryDTO dto) {
        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;
        Integer status = dto != null ? dto.getStatus() : null;
        String orderSn = dto != null ? dto.getOrderSn() : null;

        Page<OmsOrder> orderPage = orderServiceBiz.pageByMerchant(merchantId, status, orderSn, pageNum, pageSize);
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
}
