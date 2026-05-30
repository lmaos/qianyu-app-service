package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "提现记录项")
public class WithdrawItemVO {

    @Schema(description = "提现记录 ID")
    private Long id;

    @Schema(description = "提现单号")
    private String withdrawalNo;

    @Schema(description = "提现金额（元）")
    private String amount;

    @Schema(description = "收款银行")
    private String bankName;

    @Schema(description = "收款账号（脱敏）")
    private String bankAccount;

    @Schema(description = "收款人")
    private String accountName;

    @Schema(description = "提现状态：0~5")
    private Integer status;

    @Schema(description = "状态中文")
    private String statusText;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "申请时间")
    private String createTime;
}
