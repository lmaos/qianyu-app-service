package com.clmcat.qianyu.social.api.comment.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CommentContent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论文本内容。
     */
    private CommentContentText text;
}
