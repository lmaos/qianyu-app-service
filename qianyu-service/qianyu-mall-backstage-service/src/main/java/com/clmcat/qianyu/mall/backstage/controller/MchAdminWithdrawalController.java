package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.mch.MerchantWithdrawalApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageQueryDTO;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageResultDto;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminWithdrawalApproveDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminWithdrawalRejectDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminWithdrawalTransferDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营提现审批 Controller（资金核心·P1b）。
 * <p>类级 {@code @LoginVerify(BackstageLoginVerifyFunction) + token="X-Admin-Token"}，方法 {@code @RequiresPermission}。
 * <p>统一信封自动包装（{@code @ApiController}），返回 VO/DTO/void，禁手写 {@code ResponseEntity}/{@code Response<>}。
 * <p>状态机：0待审/1审核通过/2打款中/3打款成功/4审核拒绝/5打款失败。
 * <ul>
 *   <li>POST /page          审批列表（mch:withdrawal:view）</li>
 *   <li>POST /approve       审核通过 0→1（mch:withdrawal:approve）</li>
 *   <li>POST /reject        审核拒绝 0/1/2→4（mch:withdrawal:reject）</li>
 *   <li>POST /markTransferred 标记打款结果 2→3/5（mch:withdrawal:transfer）</li>
 * </ul>
 */
@Tag(name = "运营-提现审批", description = "提现单审批/打款标记")
@ApiController
@RequestMapping("/api/admin/withdrawal")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class MchAdminWithdrawalController {

    @DubboReference
    private MerchantWithdrawalApi withdrawalApi;

    @Operation(summary = "提现审批列表分页（平台视角跨店）")
    @RequiresPermission("mch:withdrawal:view")
    @PostMapping("/page")
    public List<WithdrawalPageResultDto> page(@Token Long adminId, @Params WithdrawalPageQueryDTO dto) {
        return withdrawalApi.pageByPlatform(dto);
    }

    @Operation(summary = "审核通过（0→1）")
    @RequiresPermission("mch:withdrawal:approve")
    @PostMapping("/approve")
    public void approve(@Token Long adminId, @Params AdminWithdrawalApproveDTO dto) {
        withdrawalApi.approve(dto.getWithdrawalId());
    }

    @Operation(summary = "审核拒绝（0/1/2→4）")
    @RequiresPermission("mch:withdrawal:reject")
    @PostMapping("/reject")
    public void reject(@Token Long adminId, @Params AdminWithdrawalRejectDTO dto) {
        withdrawalApi.reject(dto.getWithdrawalId(), dto.getRejectReason());
    }

    @Operation(summary = "标记打款结果（2→3成功/2→5失败）")
    @RequiresPermission("mch:withdrawal:transfer")
    @PostMapping("/markTransferred")
    public void markTransferred(@Token Long adminId, @Params AdminWithdrawalTransferDTO dto) {
        withdrawalApi.markTransferred(dto.getWithdrawalId(), dto.getTransferNo(), dto.getSuccess());
    }
}
