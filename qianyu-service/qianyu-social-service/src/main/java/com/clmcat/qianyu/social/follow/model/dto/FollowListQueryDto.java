package com.clmcat.qianyu.social.follow.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "关注或粉丝列表分页查询参数")
public class FollowListQueryDto {
    /**
     * 查询哪个用户的关注/粉丝列表
     */
    @Schema(description = "被查询用户ID")
    private Long userId;
    /**
     * 游标ID，倒序分页
     */
    @Schema(description = "倒序分页游标ID")
    private Long nextId;
    /**
     * 页大小
     */
    @Schema(description = "分页大小")
    private Integer limit;
}
