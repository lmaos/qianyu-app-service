package com.clmcat.qianyu.social.api.like.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 点赞记录ID。
     */
    private Long id;

    /**
     * 评论ID。
     */
    private Long commentId;

    /**
     * 所属作品ID。
     */
    private Long momentId;

    /**
     * 评论作者ID。
     */
    private Long authorId;

    /**
     * 点赞用户ID。
     */
    private Long userId;

    /**
     * 客户端时间戳。
     */
    private Long clientTime;
}
