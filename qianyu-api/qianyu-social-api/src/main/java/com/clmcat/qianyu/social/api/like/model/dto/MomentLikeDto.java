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
public class MomentLikeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 点赞记录ID。
     */
    private Long id;

    /**
     * 作品ID。
     */
    private Long momentId;

    /**
     * 作品作者ID。
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
