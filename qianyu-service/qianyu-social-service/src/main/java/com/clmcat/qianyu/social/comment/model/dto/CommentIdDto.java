package com.clmcat.qianyu.social.comment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "评论ID参数")
public class CommentIdDto {
    /**
     * 评论ID。
     */
    @Schema(description = "评论ID")
    private Long commentId;
}
