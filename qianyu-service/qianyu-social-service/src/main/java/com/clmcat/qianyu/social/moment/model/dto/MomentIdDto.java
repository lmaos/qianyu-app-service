package com.clmcat.qianyu.social.moment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "动态ID参数")
public class MomentIdDto {
    /**
     * 动态编号
     */
    @Schema(description = "动态ID")
    private Long momentId;
}
