package com.clmcat.qianyu.social.follow.model.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * 常用联系人（好友）展示项。
 * <p>
 * 由 {@code FollowServiceBiz#getFriendIdsByUserId} 取好友 ID，再批量查询用户信息富化得到。
 */
@Getter
@Builder
public class ContactVo {
    /** 用户 ID */
    private Long userId;
    /** 昵称（批量查询用户信息填充） */
    private String nickname;
    /** 头像 URL（批量查询用户信息填充） */
    private String avatar;
    /** 用户外显 ID（userNo） */
    private String userNo;
}
