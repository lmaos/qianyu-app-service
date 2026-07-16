package com.clmcat.qianyu.mall.oms.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus;
import com.clmcat.qianyu.mall.oms.model.dto.*;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleSimpleVO;
import com.clmcat.qianyu.mall.oms.model.vo.OrderDetailVO;
import com.clmcat.qianyu.mall.oms.model.vo.OrderSimpleVO;
import com.clmcat.qianyu.mall.oms.service.OmsAfterSaleViewServiceBiz;
import com.clmcat.qianyu.mall.oms.service.OmsOrderViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "OMS-订单(B端)")
@ApiController
@RequestMapping("/api/mall/merchant/oms")
@LoginVerify
public class OmsMerchantOrderController {

    @Resource
    private OmsOrderViewServiceBiz orderViewServiceBiz;

    @Resource
    private OmsAfterSaleViewServiceBiz afterSaleViewServiceBiz;

    @DubboReference
    private MerchantApi merchantApi;

    @Operation(summary = "B端-订单列表")
    @PostMapping("/orderList")
    public Page<OrderSimpleVO> merchantOrderList(
            @Parameter(hidden = true) @Token long userId,
            @Params OrderQueryDTO dto) {
        // TODO: resolve merchantId from userId via RPC
        Long merchantId = resolveMerchantId(userId);
        return orderViewServiceBiz.merchantOrderList(merchantId, dto);
    }

    @Operation(summary = "B端-订单详情")
    @PostMapping("/orderDetail")
    public OrderDetailVO merchantOrderDetail(
            @Parameter(hidden = true) @Token long userId,
            @Params OrderIdDTO dto) {
        Long merchantId = resolveMerchantId(userId);
        return orderViewServiceBiz.merchantOrderDetail(merchantId, dto.getOrderId());
    }

    @Operation(summary = "B端-订单发货")
    @PostMapping("/orderShip")
    public void orderShip(@Parameter(hidden = true) @Token long userId, @Params OrderShipDTO dto) {
        Long merchantId = resolveMerchantId(userId);
        orderViewServiceBiz.shipOrder(merchantId, dto);
    }

    @Operation(summary = "B端-售后审批")
    @PostMapping("/aftersaleAudit")
    public void aftersaleAudit(@Parameter(hidden = true) @Token long userId, @Params AfterSaleAuditDTO dto) {
        Long merchantId = resolveMerchantId(userId);
        afterSaleViewServiceBiz.auditAfterSale(merchantId, dto);
    }

    @Operation(summary = "B端-售后列表")
    @PostMapping("/aftersaleList")
    public Page<AfterSaleSimpleVO> aftersaleList(
            @Parameter(hidden = true) @Token long userId,
            @Params AfterSaleQueryDTO dto) {
        Long merchantId = resolveMerchantId(userId);
        return afterSaleViewServiceBiz.merchantAfterSaleList(merchantId, dto);
    }

    @Operation(summary = "B端-确认收到退货（type=2 退货退款/type=3换货/type=4维修）")
    @PostMapping("/aftersaleConfirmReturn")
    public void aftersaleConfirmReturn(
            @Parameter(hidden = true) @Token long userId,
            @Params AfterSaleIdDTO dto) {
        Long merchantId = resolveMerchantId(userId);
        afterSaleViewServiceBiz.aftersaleConfirmReturn(merchantId, dto.getAftersaleId());
    }

    @Operation(summary = "B端-商家填写寄回物流（type=3换货/type=4维修，55→70）")
    @PostMapping("/aftersaleSendBack")
    public void aftersaleSendBack(
            @Parameter(hidden = true) @Token long userId,
            @Params AfterSaleReturnShipDTO dto) {
        Long merchantId = resolveMerchantId(userId);
        afterSaleViewServiceBiz.aftersaleSendBack(merchantId, dto);
    }

    private Long resolveMerchantId(Long userId) {
        // 商户身份门禁：必须已审核通过且生效（待审/冻结/禁用一律拒绝）——统一走 requireActiveMerchant
        return merchantApi.requireActiveMerchant(userId).getId();
    }
}
