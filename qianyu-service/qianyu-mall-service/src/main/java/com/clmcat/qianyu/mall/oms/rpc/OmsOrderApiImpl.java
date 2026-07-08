package com.clmcat.qianyu.mall.oms.rpc;

import com.clmcat.qianyu.mall.api.oms.OmsOrderApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.api.oms.model.dto.OrderPageQueryDTO;
import com.clmcat.qianyu.mall.oms.mapper.OmsOrderItemMapper;
import com.clmcat.qianyu.mall.oms.mapper.OmsOrderMapper;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrder;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrderItem;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Service
public class OmsOrderApiImpl implements OmsOrderApi {

    @Resource
    private OmsOrderMapper orderMapper;

    @Resource
    private OmsOrderItemMapper orderItemMapper;

    @Transactional(rollbackFor = Exception.class)
    public void insertOrderWithItems(OmsOrder order, List<OmsOrderItem> items) {
        orderMapper.insertSelective(order);
        orderItemMapper.insertBatch(items);
    }

    @Override
    public OmsOrderDto findById(Long orderId) {
        OmsOrder order = orderMapper.selectOneById(orderId);
        return toDto(order);
    }

    @Override
    public OmsOrderDto findByOrderNo(String orderNo) {
        OmsOrder order = orderMapper.selectOneByQuery(
                QueryWrapper.create().where("order_no = ?", orderNo));
        return toDto(order);
    }

    @Override
    public boolean updateWithOptimisticLock(OmsOrderDto dto) {
        OmsOrder order = orderMapper.selectOneById(dto.getId());
        if (order == null || !order.getVersion().equals(dto.getVersion())) {
            return false;
        }
        // Increment version for optimistic lock
        order.setVersion(order.getVersion() + 1);
        // Update fields
        if (dto.getStatus() != null) order.setStatus(dto.getStatus());
        if (dto.getAfterSaleStatus() != null) order.setAfterSaleStatus(dto.getAfterSaleStatus());
        if (dto.getAfterSaleType() != null) order.setAfterSaleType(dto.getAfterSaleType());
        order.setUpdateTime(System.currentTimeMillis());
        return orderMapper.update(order) > 0;
    }

    @Override
    public boolean transitStatus(Long orderId, int fromStatus, int toStatus) {
        OmsOrder order = orderMapper.selectOneById(orderId);
        if (order == null || order.getStatus() != fromStatus) {
            return false;
        }
        long ver = order.getVersion();
        // P0-3: 真 CAS — WHERE id + status + version，防并发双推进
        OmsOrder update = new OmsOrder();
        update.setStatus(toStatus);
        update.setVersion(ver + 1);
        update.setUpdateTime(System.currentTimeMillis());
        int affected = orderMapper.updateByQuery(update,
                QueryWrapper.create().where("id = ?", orderId)
                        .and("status = ?", fromStatus)
                        .and("version = ?", ver));
        return affected > 0;
    }

    @Override
    public List<OmsOrderDto> pageByPlatform(OrderPageQueryDTO query) {
        // 动态过滤：所有占位符由 MyBatis-Flex 参数化，杜绝 SQL 拼接
        QueryWrapper qw = QueryWrapper.create().where("deleted = ?", 0);
        if (query.getMerchantId() != null) {
            qw.and("merchant_id = ?", query.getMerchantId());
        }
        if (query.getStatus() != null) {
            qw.and("status = ?", query.getStatus());
        }
        if (query.getOrderNo() != null && !query.getOrderNo().isEmpty()) {
            qw.and("order_no like ?", "%" + query.getOrderNo() + "%");
        }
        if (query.getBuyerUserId() != null) {
            qw.and("user_id = ?", query.getBuyerUserId());
        }
        qw.orderBy("create_time DESC");

        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;

        com.mybatisflex.core.paginate.Page<OmsOrder> page =
                orderMapper.paginate(com.mybatisflex.core.paginate.Page.of(pageNum, pageSize), qw);
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return Collections.emptyList();
        }
        return page.getRecords().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<OmsOrderItem> findItemsByOrderId(Long orderId) {
        return orderItemMapper.selectListByQuery(
                QueryWrapper.create().where("order_id = ?", orderId));
    }

    public OmsOrderItem findItemById(Long orderItemId) {
        return orderItemMapper.selectOneById(orderItemId);
    }

    public com.mybatisflex.core.paginate.Page<OmsOrder> pageByUser(
            Long userId, Integer status, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .where("user_id = ?", userId).and("deleted = 0");
        if (status != null && status != 0) {
            query.and("status = ?", status);
        }
        return orderMapper.paginate(pageNum, pageSize, query);
    }

    public com.mybatisflex.core.paginate.Page<OmsOrder> pageByMerchant(
            Long merchantId, Integer status, String orderSn, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .where("merchant_id = ?", merchantId).and("deleted = 0");
        if (status != null && status != 0) {
            query.and("status = ?", status);
        }
        if (orderSn != null && !orderSn.isEmpty()) {
            query.and("order_no = ?", orderSn);
        }
        return orderMapper.paginate(pageNum, pageSize, query);
    }

    /**
     * Internal helper - get order entity by ID
     */
    public OmsOrder getOrderById(Long orderId) {
        return orderMapper.selectOneById(orderId);
    }

    /**
     * Internal helper - update order entity
     */
    public void updateOrder(OmsOrder order) {
        orderMapper.update(order);
    }

    private OmsOrderDto toDto(OmsOrder order) {
        if (order == null) return null;
        OmsOrderDto dto = new OmsOrderDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setMerchantId(order.getMerchantId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setFreightAmount(order.getFreightAmount());
        dto.setCouponAmount(order.getCouponAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalQuantity(order.getTotalQuantity());
        dto.setStatus(order.getStatus());
        dto.setAfterSaleStatus(order.getAfterSaleStatus());
        dto.setAfterSaleType(order.getAfterSaleType());
        dto.setVersion(order.getVersion());
        dto.setSource(order.getSource());
        dto.setBuyerMessage(order.getBuyerMessage());
        dto.setPayTime(order.getPayTime());
        dto.setDeliveryTime(order.getDeliveryTime());
        dto.setReceiveTime(order.getReceiveTime());
        dto.setCloseTime(order.getCloseTime());
        dto.setCreateTime(order.getCreateTime());
        return dto;
    }
}
