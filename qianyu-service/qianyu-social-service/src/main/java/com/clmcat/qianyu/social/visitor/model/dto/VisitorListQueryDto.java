package com.clmcat.qianyu.social.visitor.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访客列表或浏览历史列表分页查询参数。
 */
@Data
@Schema(description = "访客列表或浏览历史列表分页查询参数")
public class VisitorListQueryDto {

    /** 被查询用户ID（查访客列表时为主页主人，查历史列表时为访问者） */
    @Schema(description = "被查询用户ID")
    private Long userId;

    /** 游标ID，倒序分页（雪花ID） */
    @Schema(description = "倒序分页游标：上一页最后一条的雪花ID")
    private Long nextId;

    /** 页大小 */
    @Schema(description = "分页大小")
    private Integer limit;
}
