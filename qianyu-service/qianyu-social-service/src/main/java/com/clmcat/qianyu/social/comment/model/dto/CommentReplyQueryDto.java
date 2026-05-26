package com.clmcat.qianyu.social.comment.model.dto;

import lombok.Data;

@Data
public class CommentReplyQueryDto {
    /**
     * 父评论ID（一级评论ID）。
     */
    private Long parentCommentId;

    /**
     * 游标评论ID。
     */
    private Long nextCommentId;

    /**
     * 页大小。
     */
    private Integer limit;
}
