package com.clmcat.qianyu.social.like.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "作品点赞参数")
public class LikeMomentTargetDto {
    /**
     * 作品ID。
     */
    @Schema(description = "作品ID")
    private Long momentId;
}
