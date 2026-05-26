package com.clmcat.qianyu.social.comment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "评论回复分页查询参数")
public class CommentReplyQueryDto {
    /**
     * 父评论ID（一级评论ID）。
     */
    @Schema(description = "一级评论ID")
    private Long parentCommentId;

    /**
     * 游标评论ID。
     */
    @Schema(description = "倒序分页游标评论ID")
    private Long nextCommentId;

    /**
     * 页大小。
     */
    @Schema(description = "分页大小")
    private Integer limit;
}
