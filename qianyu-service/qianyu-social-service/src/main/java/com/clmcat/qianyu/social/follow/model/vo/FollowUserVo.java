package com.clmcat.qianyu.social.follow.model.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowUserVo {
    private Long id;
    private Long userId;
    private Long clientTime;
}
