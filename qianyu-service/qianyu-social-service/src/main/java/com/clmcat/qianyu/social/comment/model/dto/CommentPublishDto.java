package com.clmcat.qianyu.social.comment.model.dto;

import com.clmcat.qianyu.social.api.comment.model.dto.CommentContent;
import lombok.Data;

@Data
public class CommentPublishDto {
    /**
     * 作品ID。
     */
    private Long momentId;

    /**
     * 父评论ID；顶级评论为0。
     */
    private Long parentCommentId;

    /**
     * 实际回复的评论ID；顶级评论为0。
     */
    private Long replyCommentId;

    /**
     * 评论内容。
     */
    private CommentContent content;
}
