package com.clmcat.qianyu.social.visitor.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访客数量查询参数。
 */
@Data
@Schema(description = "访客数量查询参数")
public class VisitorUserQueryDto {

    /** 被查询用户ID */
    @Schema(description = "被查询用户ID")
    private Long userId;
}
