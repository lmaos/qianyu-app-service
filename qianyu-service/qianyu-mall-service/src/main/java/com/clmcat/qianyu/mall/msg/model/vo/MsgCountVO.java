package com.clmcat.qianyu.mall.msg.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/** 未读数。 */
@Data
@Builder
@Schema(description = "未读通知数")
public class MsgCountVO {
    @Schema(description = "未读条数")
    private Long count;
}
