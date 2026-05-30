package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "结算单项")
public class SettlementItemVO {

    @Schema(description = "结算单 ID")
    private Long id;

    @Schema(description = "结算单号")
    private String settlementNo;

    @Schema(description = "结算周期开始")
    private String startTime;

    @Schema(description = "结算周期结束")
    private String endTime;

    @Schema(description = "本期订单笔数")
    private Integer orderCount;

    @Schema(description = "本期订单总金额（元）")
    private String orderAmount;

    @Schema(description = "本期退款笔数")
    private Integer refundCount;

    @Schema(description = "本期退款总金额（元）")
    private String refundAmount;

    @Schema(description = "本期平台佣金（元）")
    private String platformFee;

    @Schema(description = "本期主播佣金（元）")
    private String anchorFee;

    @Schema(description = "本期应结金额（元）")
    private String settlementAmount;

    @Schema(description = "结算状态：0=待结算 1=已结算 2=已打款")
    private Integer status;

    @Schema(description = "结算时间")
    private String settleTime;
}
