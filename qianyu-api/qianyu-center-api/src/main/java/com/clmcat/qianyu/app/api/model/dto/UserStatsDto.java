package com.clmcat.qianyu.app.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户统计数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 获赞数 */
    private Long likeCount;

    /** 关注数 */
    private Long followCount;

    /** 粉丝数 */
    private Long fansCount;

    /** 新访客数 */
    private Long visitorCount;
}
