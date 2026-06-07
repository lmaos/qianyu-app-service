package com.clmcat.qianyu.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "按 userNo 搜索用户参数")
public class UserNoSearchDto {

    /**
     * 用户外显 ID（搜索、分享用的业务 ID，全局唯一）。
     */
    @Schema(description = "用户外显ID，用于按 userNo 精确搜索用户")
    private String userNo;
}
