package com.clmcat.qianyu.mall.mch.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.mch.model.dto.BillQueryDTO;
import com.clmcat.qianyu.mall.mch.model.dto.SettlementQueryDTO;
import com.clmcat.qianyu.mall.mch.model.dto.WithdrawApplyDTO;
import com.clmcat.qianyu.mall.mch.model.dto.WithdrawQueryDTO;
import com.clmcat.qianyu.mall.mch.model.vo.AccountInfoVO;
import com.clmcat.qianyu.mall.mch.model.vo.BillItemVO;
import com.clmcat.qianyu.mall.mch.model.vo.SettlementItemVO;
import com.clmcat.qianyu.mall.mch.model.vo.WithdrawItemVO;
import com.clmcat.qianyu.mall.mch.service.MerchantAccountViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "商家账户资金", description = "账户、账单、结算、提现")
@ApiController
@RequestMapping("/api/mall/merchant/mch")
// @LoginVerify
public class MerchantAccountController {

    @Resource
    private MerchantAccountViewServiceBiz accountViewServiceBiz;

    /**
     * 账户信息查询
     */
    @Operation(summary = "账户信息查询")
    @PostMapping("/accountInfo")
    public AccountInfoVO accountInfo(@Parameter(hidden = true) @Token long userId) {
        return accountViewServiceBiz.getAccountInfo(userId);
    }

    /**
     * 账单列表
     */
    @Operation(summary = "账单列表")
    @PostMapping("/billList")
    public Page<BillItemVO> billList(
            @Parameter(hidden = true) @Token long userId,
            @Params BillQueryDTO dto) {
        return accountViewServiceBiz.getBillList(userId, dto);
    }

    /**
     * 结算单列表
     */
    @Operation(summary = "结算单列表")
    @PostMapping("/settlementList")
    public Page<SettlementItemVO> settlementList(
            @Parameter(hidden = true) @Token long userId,
            @Params SettlementQueryDTO dto) {
        return accountViewServiceBiz.getSettlementList(userId, dto);
    }

    /**
     * 提现申请
     */
    @Operation(summary = "提现申请")
    @PostMapping("/withdrawApply")
    public String withdrawApply(
            @Parameter(hidden = true) @Token long userId,
            @Params WithdrawApplyDTO dto) {
        return accountViewServiceBiz.withdrawApply(userId, dto);
    }

    /**
     * 提现记录列表
     */
    @Operation(summary = "提现记录列表")
    @PostMapping("/withdrawList")
    public Page<WithdrawItemVO> withdrawList(
            @Parameter(hidden = true) @Token long userId,
            @Params WithdrawQueryDTO dto) {
        return accountViewServiceBiz.getWithdrawList(userId, dto);
    }
}
