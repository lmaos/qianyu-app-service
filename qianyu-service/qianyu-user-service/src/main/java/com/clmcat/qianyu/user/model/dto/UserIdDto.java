package com.clmcat.qianyu.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户ID查询参数")
public class UserIdDto {

    /**
     * 目标用户ID。
     */
    @Schema(description = "目标用户ID")
    private Long targetId;
}
