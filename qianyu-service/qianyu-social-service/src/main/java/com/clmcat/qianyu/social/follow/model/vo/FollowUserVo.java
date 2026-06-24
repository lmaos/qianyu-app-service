package com.clmcat.qianyu.social.follow.model.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowUserVo {
    /** 关注关系记录 ID（雪花 ID） */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 是否互关好友：0 否，1 是 */
    private Integer isFriend;
    /** 关注时间戳（Unix 毫秒） */
    private Long clientTime;
    /** 用户昵称（批量查询用户信息填充） */
    private String nickname;
    /** 用户头像 URL（批量查询用户信息填充） */
    private String avatar;
}
