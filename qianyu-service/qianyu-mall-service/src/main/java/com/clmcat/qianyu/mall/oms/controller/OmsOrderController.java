package com.clmcat.qianyu.mall.oms.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.oms.model.dto.*;
import com.clmcat.qianyu.mall.oms.model.vo.OrderCreateVO;
import com.clmcat.qianyu.mall.oms.model.vo.OrderDetailVO;
import com.clmcat.qianyu.mall.oms.model.vo.OrderSimpleVO;
import com.clmcat.qianyu.mall.oms.service.OmsOrderViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "OMS-订单(C端)")
@ApiController
@RequestMapping("/api/mall/oms")
@LoginVerify
public class OmsOrderController {

    @Resource
    private OmsOrderViewServiceBiz orderViewServiceBiz;

    @Operation(summary = "下单")
    @PostMapping("/orderCreate")
    public OrderCreateVO orderCreate(@Parameter(hidden = true) @Token long userId, @Params OrderCreateDTO dto) {
        return orderViewServiceBiz.createOrder(userId, dto);
    }

    // app.md §11.1 /api/mall/oms/orderList
    @Operation(summary = "订单列表")
    @PostMapping("/orderList")
    public Page<OrderSimpleVO> orderList(@Parameter(hidden = true) @Token long userId, @Params OrderQueryDTO dto) {
        return orderViewServiceBiz.orderList(userId, dto);
    }

    // app.md §11.2 /api/mall/oms/orderDetail
    @Operation(summary = "订单详情")
    @PostMapping("/orderDetail")
    public OrderDetailVO orderDetail(@Parameter(hidden = true) @Token long userId, @Params OrderIdDTO dto) {
        return orderViewServiceBiz.orderDetail(userId, dto.getOrderId());
    }

    // app.md §11.4 /api/mall/oms/orderCancel
    @Operation(summary = "取消订单")
    @PostMapping("/orderCancel")
    public void orderCancel(@Parameter(hidden = true) @Token long userId, @Params OrderCancelDTO dto) {
        orderViewServiceBiz.cancelOrder(userId, dto);
    }

    // app.md §11.4 /api/mall/oms/orderReceive
    @Operation(summary = "确认收货")
    @PostMapping("/orderReceive")
    public void orderReceive(@Parameter(hidden = true) @Token long userId, @Params OrderIdDTO dto) {
        orderViewServiceBiz.receiveOrder(userId, dto.getOrderId());
    }

    // app.md §11.3 /api/mall/oms/orderDelete
    @Operation(summary = "删除订单")
    @PostMapping("/orderDelete")
    public void orderDelete(@Parameter(hidden = true) @Token long userId, @Params OrderIdDTO dto) {
        orderViewServiceBiz.deleteOrder(userId, dto.getOrderId());
    }
}
