package com.clmcat.qianyu.social.moment.model.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MomentAuthorPageVo {
    private Long authorId;
    private Long nextMomentId;
    private boolean hasMore;
    private List<MomentVo> datas;
}
