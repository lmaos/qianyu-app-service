package com.clmcat.qianyu.social.like.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "评论点赞参数")
public class LikeCommentTargetDto {
    /**
     * 评论ID。
     */
    @Schema(description = "评论或回复ID")
    private Long commentId;
}
