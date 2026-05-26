package com.clmcat.qianyu.social.comment.model.dto;

import lombok.Data;

@Data
public class CommentMomentQueryDto {
    /**
     * 作品ID。
     */
    private Long momentId;

    /**
     * 游标评论ID。
     */
    private Long nextCommentId;

    /**
     * 页大小。
     */
    private Integer limit;
}
