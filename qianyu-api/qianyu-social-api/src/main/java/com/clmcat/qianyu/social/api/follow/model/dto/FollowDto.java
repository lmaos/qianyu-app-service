package com.clmcat.qianyu.social.api.follow.model.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"followerId", "followeeId"})
public class FollowDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 关系记录编号（雪花）
     */
    private Long id;

    private Long followerId;

    private Long followeeId;

    /**
     * 是否互关好友：0否，1是。
     */
    private Integer isFriend;

    /**
     * 客户端时间戳
     */
    private Long clientTime;
}
