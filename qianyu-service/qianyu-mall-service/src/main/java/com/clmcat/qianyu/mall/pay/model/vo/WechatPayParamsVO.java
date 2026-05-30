package com.clmcat.qianyu.mall.pay.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "微信支付参数")
public class WechatPayParamsVO {

    @Schema(description = "微信 AppID")
    private String appId;

    @Schema(description = "时间戳")
    private String timeStamp;

    @Schema(description = "随机字符串")
    private String nonceStr;

    @Schema(description = "预支付参数")
    private String packageValue;

    @Schema(description = "签名类型")
    private String signType;

    @Schema(description = "签名")
    private String paySign;
}
