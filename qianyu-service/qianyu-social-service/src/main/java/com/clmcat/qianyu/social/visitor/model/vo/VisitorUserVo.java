package com.clmcat.qianyu.social.visitor.model.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * 访客/浏览历史用户 VO。
 */
@Getter
@Builder
public class VisitorUserVo {

    /** 访客记录 ID（雪花 ID） */
    private Long id;

    /** 对方用户 ID */
    private Long userId;

    /** 累计访问次数 */
    private Integer visitCount;

    /** 最近访问时间戳（Unix 毫秒） */
    private Long clientTime;

    /** 用户昵称（批量查询用户信息填充） */
    private String nickname;

    /** 用户头像 URL（批量查询用户信息填充） */
    private String avatar;
}
