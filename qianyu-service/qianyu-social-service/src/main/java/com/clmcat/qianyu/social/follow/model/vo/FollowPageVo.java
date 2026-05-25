package com.clmcat.qianyu.social.follow.model.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FollowPageVo {
    private Long userId;
    private Long nextId;
    private boolean hasMore;
    private List<FollowUserVo> followList;
}
