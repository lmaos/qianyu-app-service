package com.clmcat.qianyu.social.comment.model.dto;

import com.clmcat.qianyu.social.api.comment.model.dto.CommentContent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "评论发布参数")
public class CommentPublishDto {
    /**
     * 作品ID。
     */
    @Schema(description = "作品ID")
    private Long momentId;

    /**
     * 父评论ID；顶级评论为0。
     */
    @Schema(description = "父评论ID；顶级评论传 0")
    private Long parentCommentId;

    /**
     * 实际回复的评论ID；顶级评论为0。
     */
    @Schema(description = "实际回复的评论ID；顶级评论传 0")
    private Long replyCommentId;

    /**
     * 评论内容。
     */
    @Schema(description = "评论内容")
    private CommentContent content;
}
