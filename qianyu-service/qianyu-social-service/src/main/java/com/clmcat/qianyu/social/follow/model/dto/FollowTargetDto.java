package com.clmcat.qianyu.social.follow.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "关注目标参数")
public class FollowTargetDto {
    /**
     * 目标用户ID
     */
    @Schema(description = "目标用户ID，用于关注、取消关注、关系查询等面向目标用户的接口")
    private Long targetId;
}
