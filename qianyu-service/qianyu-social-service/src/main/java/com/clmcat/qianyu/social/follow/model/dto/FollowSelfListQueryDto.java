package com.clmcat.qianyu.social.follow.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "当前登录用户自己的关注/粉丝列表分页查询参数")
public class FollowSelfListQueryDto {
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
