package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.mch.MerchantAccountApi;
import com.clmcat.qianyu.mall.api.mch.MerchantWithdrawalApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantAccountDto;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageQueryDTO;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageResultDto;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminMerchantIdDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营结算对账 Controller（编码运营 Phase 3·结算对账）。
 *
 * <p>类级 {@code @LoginVerify(BackstageLoginVerifyFunction) + token="X-Admin-Token"}，
 * 方法 {@code @RequiresPermission}。
 * <p>统一信封自动包装（{@code @ApiController}），返回 DTO/VO，禁手写 {@code ResponseEntity}/{@code Response<>}。
 *
 * <p>运营端查指定商户的资金对账数据：
 * <ul>
 *   <li>POST /account     商户账户概览（balance/frozen/totalIncome/totalWithdraw/totalCommission/version）</li>
 *   <li>POST /withdrawals 指定商户的提现列表（按 merchantId 过滤）</li>
 * </ul>
 *
 * <p>账单/结算明细（{@code getSettlementList/getBillList}）目前为商家端 ViewBiz，不走 Dubbo，
 * 运营端本期暂不开放；后续如需对账明细可下沉为 RPC 契约。
 */
@Tag(name = "运营-结算对账", description = "商户资金账户/提现列表对账")
@ApiController
@RequestMapping("/api/admin/finance")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class AdminFinanceController {

    @DubboReference
    private MerchantAccountApi merchantAccountApi;

    @DubboReference
    private MerchantWithdrawalApi withdrawalApi;

    /**
     * 商户账户概览（结算对账）。
     * <p>返回指定商户的账户余额/冻结/累计收支/佣金/版本号，供运营核对资金。
     *
     * @param adminId 当前运营账号 ID（@Token 注入）
     * @param dto     请求体 {@code {merchantId}}
     * @return 商户账户 DTO（balance/frozenAmount/totalIncome/totalWithdraw/totalRefund/totalCommission/version）
     */
    @Operation(summary = "商户账户概览（结算对账）")
    @RequiresPermission("mch:withdrawal:view")
    @PostMapping("/account")
    public MerchantAccountDto account(@Token Long adminId, @Params AdminMerchantIdDTO dto) {
        return merchantAccountApi.getByMerchantId(dto.getMerchantId());
    }

    /**
     * 指定商户的提现列表（结算对账）。
     * <p>按 {@code merchantId} 过滤该商户的全部提现单（status 任意），按申请时间倒序。
     *
     * @param adminId 当前运营账号 ID（@Token 注入）
     * @param dto     请求体 {@code {merchantId}}
     * @return 该商户的提现单列表（含四要素脱敏 + accountBalance + allowedActions 富化）
     */
    @Operation(summary = "商户提现列表（结算对账）")
    @RequiresPermission("mch:withdrawal:view")
    @PostMapping("/withdrawals")
    public List<WithdrawalPageResultDto> withdrawals(@Token Long adminId, @Params AdminMerchantIdDTO dto) {
        WithdrawalPageQueryDTO query = new WithdrawalPageQueryDTO();
        query.setMerchantId(dto.getMerchantId());
        // 对账场景需看全量，给较大 pageSize；与 pageByPlatform 实现一致（缺省 1/10）
        query.setPageNum(1);
        query.setPageSize(100);
        return withdrawalApi.pageByPlatform(query);
    }
}
