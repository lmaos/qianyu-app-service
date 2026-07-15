package com.clmcat.qianyu.mall.msg.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 按类型的通知请求（未读数 / 全部已读）。 */
@Data
@Schema(description = "按类型的通知请求")
public class MsgTypeDTO {
    @Schema(description = "通知类型：1=商户 2=订单 3=支付 4=售后 5=系统；空=全部")
    private Integer type;
}
