package com.clmcat.qianyu.social.visitor.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 当前登录用户自己的访客列表或浏览历史列表分页查询参数。
 */
@Data
@Schema(description = "当前登录用户自己的访客列表或浏览历史列表分页查询参数")
public class VisitorSelfListQueryDto {

    /** 游标ID，倒序分页（雪花ID） */
    @Schema(description = "倒序分页游标：上一页最后一条的雪花ID")
    private Long nextId;

    /** 页大小 */
    @Schema(description = "分页大小")
    private Integer limit;
}
