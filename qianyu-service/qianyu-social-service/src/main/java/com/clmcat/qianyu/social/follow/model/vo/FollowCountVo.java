package com.clmcat.qianyu.social.follow.model.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowCountVo {
    private Long userId;
    private Long followCount;
    private Long followerCount;
}
