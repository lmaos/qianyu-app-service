package com.clmcat.qianyu.social.api.follow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowRelationDto {
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
