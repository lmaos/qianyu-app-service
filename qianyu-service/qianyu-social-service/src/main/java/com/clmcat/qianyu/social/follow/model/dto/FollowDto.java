package com.clmcat.qianyu.social.follow.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"followerId", "followeeId"})
public class FollowDto {

    /**
     * 用户自己
     */
    private Long followerId;  // 关注者，即“我”
    /**
     * 关注了这个人
     */
    private Long followeeId;  // 被关注者，即“他”

    /**
     * 是否互关好友：0否，1是。
     */
    private Integer isFriend;
}