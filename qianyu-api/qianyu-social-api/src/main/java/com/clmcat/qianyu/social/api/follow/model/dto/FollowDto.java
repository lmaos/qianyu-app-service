package com.clmcat.qianyu.social.api.follow.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"followerId", "followeeId"})
public class FollowDto {
    /**
     * 关系记录编号（雪花）
     */
    private Long id;

    private Long followerId;

    private Long followeeId;

    /**
     * 客户端时间戳
     */
    private Long clientTime;
}
