package com.clmcat.qianyu.mall.oms.service;

import com.clmcat.qianyu.mall.oms.model.dto.*;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrder;
import com.clmcat.qianyu.mall.oms.model.vo.*;
import com.mybatisflex.core.paginate.Page;

public interface OmsOrderViewServiceBiz {

    OrderCreateVO createOrder(Long userId, OrderCreateDTO dto);

    Page<OrderSimpleVO> orderList(Long userId, OrderQueryDTO dto);

    OrderDetailVO orderDetail(Long userId, Long orderId);

    void cancelOrder(Long userId, OrderCancelDTO dto);

    void receiveOrder(Long userId, Long orderId);

    void deleteOrder(Long userId, Long orderId);

    Page<OrderSimpleVO> merchantOrderList(Long merchantId, OrderQueryDTO dto);

    OrderDetailVO merchantOrderDetail(Long merchantId, Long orderId);

    void shipOrder(Long merchantId, OrderShipDTO dto);

    OmsOrder getOrderById(Long orderId);

}