package com.clmcat.qianyu.mall.msg.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 单条通知定位（标记已读）。 */
@Data
@Schema(description = "单条通知定位请求")
public class MsgIdDTO {
    @Schema(description = "通知ID")
    private Long messageId;
}
