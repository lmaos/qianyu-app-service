package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "提现申请请求")
public class WithdrawApplyDTO {

    @Schema(description = "提现金额（元）")
    private String amount;

    @Schema(description = "收款银行名称")
    private String bankName;

    @Schema(description = "收款银行账号")
    private String bankAccount;

    @Schema(description = "收款人姓名")
    private String accountName;
}
