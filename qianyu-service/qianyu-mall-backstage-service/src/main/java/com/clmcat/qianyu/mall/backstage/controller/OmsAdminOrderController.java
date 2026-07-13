package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.oms.OmsOrderApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.api.oms.model.dto.OrderPageQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminOrderCloseDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营订单管理 Controller。
 * <p>类级 @LoginVerify(BackstageLoginVerifyFunction) + 方法 @RequiresPermission。
 * <p>跨店分页查询 + 关闭订单。架构红线：admin → mall 一律走 Dubbo RPC（@DubboReference）。
 *
 * <p>订单状态字典（与 qianyu-mall-service 的 OmsOrder 实体常量一致；本模块只依赖 mall-api，
 * 故以字面量表达，避免反向依赖 service）：
 * <pre>
 *   10 = STATUS_PENDING_PAY  待付款（运营关闭的合法源态）
 *   20 = STATUS_PENDING_SHIP 待发货
 *   30 = STATUS_SHIPPED      已发货
 *   40 = STATUS_COMPLETED    已完成
 *   50 = STATUS_CANCELLED    关闭/取消（运营关闭的目标态）
 *   60 = STATUS_CLOSED       已关闭
 * </pre>
 */
@Tag(name = "运营-订单管理", description = "跨店分页/关闭订单")
@ApiController
@RequestMapping("/api/admin/order")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class OmsAdminOrderController {

    /** 订单状态：待付款（运营关闭 CAS 的源态，对应 OmsOrder.STATUS_PENDING_PAY） */
    private static final int ORDER_STATUS_PENDING_PAY = 10;
    /** 订单状态：关闭/取消（运营关闭 CAS 的目标态，对应 OmsOrder.STATUS_CANCELLED） */
    private static final int ORDER_STATUS_CANCELLED = 50;

    @DubboReference
    private OmsOrderApi omsOrderApi;

    @Operation(summary = "订单列表分页（跨店）")
    @RequiresPermission("oms:order:view")
    @PostMapping("/page")
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<OmsOrderDto> page(
            @Token Long adminId, @Params OrderPageQueryDTO dto) {
        return omsOrderApi.pageByPlatform(dto);
    }

    /**
     * 关闭订单。
     * <p>CAS 流转：10（待付款）→ 50（关闭/取消）。调 {@link OmsOrderApi#transitStatus}，
     * 其底层 WHERE id + status + version 防并发双推进；CAS 失败（订单不存在或状态已变更）
     * 抛 {@link ResponseStatus#R_OPERATION_FAIL}。
     *
     * @param adminId 当前运营账号 ID（@Token 注入，用于操作审计）
     * @param dto     关闭请求（orderId）
     */
    @Operation(summary = "关闭订单")
    @RequiresPermission("oms:order:close")
    @PostMapping("/close")
    public void close(@Token Long adminId, @Params AdminOrderCloseDTO dto) {
        boolean ok = omsOrderApi.transitStatus(
                dto.getOrderId(), ORDER_STATUS_PENDING_PAY, ORDER_STATUS_CANCELLED);
        ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(
                "订单关闭失败（订单不存在或状态已变更）", !ok);
    }
}
