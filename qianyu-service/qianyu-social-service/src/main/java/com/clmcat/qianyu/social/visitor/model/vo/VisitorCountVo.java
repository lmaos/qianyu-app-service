package com.clmcat.qianyu.social.visitor.model.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * 访客数量 VO。
 */
@Getter
@Builder
public class VisitorCountVo {

    /** 用户ID */
    private Long userId;

    /** 新访客数 */
    private Long visitorCount;
}
