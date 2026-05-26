package com.clmcat.qianyu.social.comment.model.vo;

import com.clmcat.qianyu.social.api.comment.model.dto.CommentContent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentVo {
    private Long commentId;
    private Long momentId;
    private Long momentAuthorId;
    private Long authorId;
    private Long parentCommentId;
    private Long replyCommentId;
    private Long replyUserId;
    private Integer commentLevel;
    private CommentContent content;
    private Integer status;
    private Long likes;
    private Long replies;
    private Long clientTime;
}
