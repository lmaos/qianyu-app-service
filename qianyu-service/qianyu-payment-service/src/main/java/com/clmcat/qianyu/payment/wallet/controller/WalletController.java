package com.clmcat.qianyu.payment.wallet.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.payment.wallet.model.dto.TransactionQueryDto;
import com.clmcat.qianyu.payment.wallet.model.dto.WalletOperateDto;
import com.clmcat.qianyu.payment.wallet.model.vo.TransactionListVo;
import com.clmcat.qianyu.payment.wallet.model.vo.TransactionVo;
import com.clmcat.qianyu.payment.wallet.model.vo.WalletVo;
import com.clmcat.qianyu.payment.wallet.service.WalletViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 虚拟货币钱包接口。
 * <p>
 * 提供收入、支出、查询余额和交易流水等 HTTP API。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Tag(name = "虚拟货币钱包接口", description = "提供钱包余额管理、收入、支出和交易流水查询能力。")
@ApiController
@RequestMapping("/api/payment/wallet")
public class WalletController {

    @Resource
    private WalletViewServiceBiz walletViewServiceBiz;

    @Operation(summary = "支出（扣钱）")
    @PostMapping("/deduct")
    @LoginVerify
    public TransactionVo deduct(@Parameter(hidden = true) @Token long userId,
                                 @Params(description = "支出参数") WalletOperateDto dto) {
        return walletViewServiceBiz.deduct(userId, dto);
    }

    @Operation(summary = "查询钱包余额")
    @GetMapping("/balance")
    @LoginVerify
    public WalletVo balance(@Parameter(hidden = true) @Token long userId) {
        return walletViewServiceBiz.getWallet(userId);
    }

    @Operation(summary = "交易流水（游标分页）")
    @GetMapping("/transactions")
    @LoginVerify
    public TransactionListVo transactions(@Parameter(hidden = true) @Token long userId,
                                           @ParameterObject @Params TransactionQueryDto dto) {
        if (dto == null) {
            dto = new TransactionQueryDto();
        }
        dto.setUserId(userId);
        return walletViewServiceBiz.getTransactions(dto);
    }
}
