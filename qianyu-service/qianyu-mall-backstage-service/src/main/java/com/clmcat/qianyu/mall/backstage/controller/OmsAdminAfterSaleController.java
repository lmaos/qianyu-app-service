package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.oms.OmsAfterSaleApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.AftersalePageQueryDTO;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsAfterSaleDto;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAftersaleArbitrateDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营售后介入 Controller（Phase 2）。
 * <p>类级 {@code @LoginVerify(BackstageLoginVerifyFunction) + token="X-Admin-Token"}，方法 {@code @RequiresPermission}。
 * <p>统一信封自动包装（{@code @ApiController}），返回 DTO/void，禁手写 {@code ResponseEntity}/{@code Response<>}。
 * <p>架构红线：backstage → mall 一律走 {@code @DubboReference OmsAfterSaleApi}，禁止进程内直调 impl。
 * <p>售后状态机（{@code OmsAfterSale} 常量）：10待审 / 20商家同意 / 30商家拒绝 / 40用户已发货 / 50已完成 / 60已取消。
 * <ul>
 *   <li>POST /page       跨店售后分页（oms:aftersale:view）</li>
 *   <li>POST /arbitrate  平台介入仲裁：同意→20 / 驳回→30（oms:aftersale:arbitrate）</li>
 * </ul>
 */
@Tag(name = "运营-售后介入", description = "售后分页/平台仲裁")
@ApiController
@RequestMapping("/api/admin/aftersale")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class OmsAdminAfterSaleController {

    @DubboReference
    private OmsAfterSaleApi afterSaleApi;

    @Operation(summary = "售后列表分页（平台视角跨店）")
    @RequiresPermission("oms:aftersale:view")
    @PostMapping("/page")
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<OmsAfterSaleDto> page(
            @Token Long adminId, @Params AftersalePageQueryDTO dto) {
        return afterSaleApi.pageByPlatform(dto);
    }

    /**
     * 平台介入仲裁售后单。
     * <p>{@code approved=true} → {@code updateStatusCAS(id, fromStatus, 20, null)} 同意；
     * {@code approved=false} → {@code updateStatusCAS(id, fromStatus, 30, rejectReason)} 驳回。
     * {@code fromStatus} 取当前售后单 status（CAS 防并发双推进，失败抛 {@code R_OPERATION_FAIL}）。
     */
    @Operation(summary = "平台介入仲裁（同意→20 / 驳回→30）")
    @RequiresPermission("oms:aftersale:arbitrate")
    @PostMapping("/arbitrate")
    public void arbitrate(@Token Long adminId, @Params AdminAftersaleArbitrateDTO dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("售后仲裁参数缺失", dto == null || dto.getAftersaleId() == null);
        ResponseStatus.P_NOTNULL.assertThrowResEx("approved 不能为空", dto.getApproved() == null);
        // 驳回必填原因
        ResponseStatus.P_NOTNULL.assertThrowResEx("驳回需提供 rejectReason",
                Boolean.FALSE.equals(dto.getApproved()) && (dto.getRejectReason() == null || dto.getRejectReason().isEmpty()));

        OmsAfterSaleDto afterSale = afterSaleApi.findById(dto.getAftersaleId());
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx("售后单不存在", afterSale == null);
        ResponseStatus.P_NOTNULL.assertThrowResEx("售后单状态异常", afterSale.getStatus() == null);

        int fromStatus = afterSale.getStatus();
        int toStatus = Boolean.TRUE.equals(dto.getApproved()) ? 20 : 30;
        String rejectReason = Boolean.TRUE.equals(dto.getApproved()) ? null : dto.getRejectReason();

        boolean ok = afterSaleApi.updateStatusCAS(dto.getAftersaleId(), fromStatus, toStatus, rejectReason);
        // CAS 失败：单据已被并发改动
        ResponseStatus.R_OPERATION_FAIL.assertThrowResEx("售后状态变更失败（单据已被并发改动，请刷新重试）", !ok);
    }
}
