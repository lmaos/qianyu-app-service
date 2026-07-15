package com.clmcat.qianyu.mall.msg.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 通知列表查询。 */
@Data
@Schema(description = "通知列表查询请求")
public class MsgListDTO {
    @Schema(description = "通知类型过滤：1=商户 2=订单 3=支付 4=售后 5=系统；空=全部")
    private Integer type;
    @Schema(description = "是否仅未读")
    private Boolean onlyUnread;
    @Schema(description = "页码，从1起")
    private Integer pageNum;
    @Schema(description = "每页条数")
    private Integer pageSize;
}
