package com.clmcat.qianyu.social.api.follow.model.dto;

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
public class FollowRelationDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long userId;
    private Long targetUserId;
    /**
     * userId 是否关注了 targetUserId
     */
    private boolean follow;
    /**
     * targetUserId 是否关注了 userId
     */
    private boolean follower;
    /**
     * 是否互关
     */
    private boolean friend;
}
