package com.clmcat.qianyu.social.comment.model.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentPageVo {
    private Long momentId;
    private Long parentCommentId;
    private Long nextCommentId;
    private boolean hasMore;
    private List<CommentVo> commentList;
}
