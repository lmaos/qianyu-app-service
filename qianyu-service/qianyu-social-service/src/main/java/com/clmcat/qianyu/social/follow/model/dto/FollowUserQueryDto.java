package com.clmcat.qianyu.social.follow.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户查询参数")
public class FollowUserQueryDto {
    /**
     * 被查询用户ID。
     */
    @Schema(description = "被查询用户ID，用于查询关注数、粉丝数等统计信息")
    private Long userId;
}
