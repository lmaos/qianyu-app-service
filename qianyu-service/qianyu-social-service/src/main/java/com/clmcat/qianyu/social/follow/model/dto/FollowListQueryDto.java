package com.clmcat.qianyu.social.follow.model.dto;

import lombok.Data;

@Data
public class FollowListQueryDto {
    /**
     * 查询哪个用户的关注/粉丝列表
     */
    private Long userId;
    /**
     * 游标ID，倒序分页
     */
    private Long nextId;
    /**
     * 页大小
     */
    private Integer limit;
}
