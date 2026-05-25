package com.clmcat.qianyu.social.moment.model.vo;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentContent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MomentVo {
    private Long momentId;
    private Long authorId;
    private MomentContent content;
    private double latitude;
    private double longitude;
    private String country;
    private Integer status;
    private Long createTime;
}
