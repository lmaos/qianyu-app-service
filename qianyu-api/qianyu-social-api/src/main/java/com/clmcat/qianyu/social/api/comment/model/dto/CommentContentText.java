package com.clmcat.qianyu.social.api.comment.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CommentContentText implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论文本。
     */
    private String text;

    /**
     * @ 用户ID 集合。
     */
    private List<Long> atIds;
}
