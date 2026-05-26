package com.clmcat.qianyu.social.api.comment.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CommentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID。
     */
    private Long commentId;

    /**
     * 所属作品ID。
     */
    private Long momentId;

    /**
     * 作品作者ID。
     */
    private Long momentAuthorId;

    /**
     * 评论作者ID。
     */
    private Long authorId;

    /**
     * 父评论ID；一级评论=0，二级回复=一级评论ID。
     */
    private Long parentCommentId;

    /**
     * 实际回复的那条评论ID；一级评论=0。
     */
    private Long replyCommentId;

    /**
     * 被回复用户ID；一级评论=0。
     */
    private Long replyUserId;

    /**
     * 评论层级：1=一级评论，2=二级回复。
     */
    private Integer commentLevel;

    /**
     * 评论内容。
     */
    private CommentContent content;

    /**
     * 状态：0显示，1隐藏，2删除。
     */
    private Integer status;

    /**
     * 点赞数冗余。
     */
    private Long likes;

    /**
     * 回复数冗余；仅一级评论有效。
     */
    private Long replies;

    /**
     * 客户端时间戳。
     */
    private Long clientTime;
}
