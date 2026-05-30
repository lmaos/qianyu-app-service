package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "商家账户信息")
public class AccountInfoVO {

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "可用余额（元）")
    private String balance;

    @Schema(description = "冻结金额（元）")
    private String frozenAmount;

    @Schema(description = "累计总收入（元）")
    private String totalIncome;

    @Schema(description = "累计已提现（元）")
    private String totalWithdraw;

    @Schema(description = "累计退款支出（元）")
    private String totalRefund;

    @Schema(description = "累计平台佣金（元）")
    private String totalCommission;
}
