package com.clmcat.qianyu.social.like.model.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeStatusVo {
    private Long targetId;
    private boolean liked;
}
