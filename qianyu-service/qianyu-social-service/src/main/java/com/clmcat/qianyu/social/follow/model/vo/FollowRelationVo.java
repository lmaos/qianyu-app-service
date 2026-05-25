package com.clmcat.qianyu.social.follow.model.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowRelationVo {
    private Long userId;
    private Long targetUserId;
    private boolean follow;
    private boolean follower;
    private boolean friend;
}
