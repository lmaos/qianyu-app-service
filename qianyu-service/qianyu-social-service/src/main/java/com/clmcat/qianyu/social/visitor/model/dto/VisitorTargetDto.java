package com.clmcat.qianyu.social.visitor.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访客操作目标参数。
 */
@Data
@Schema(description = "访客操作目标参数")
public class VisitorTargetDto {

    /** 目标用户ID */
    @Schema(description = "目标用户ID，用于记录访问、删除访客/历史记录等面向目标用户的接口")
    private Long targetId;
}
