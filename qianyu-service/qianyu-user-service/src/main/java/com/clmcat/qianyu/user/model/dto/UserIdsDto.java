package com.clmcat.qianyu.user.model.dto;

import com.clmcat.framework.webmvc.anns.Params;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户ID批量查询参数")
public class UserIdsDto {

    /**
     * 目标用户ID列表，适合 JSON 数组传参。
     */
    @Schema(description = "目标用户ID列表，适合 JSON 数组传参")
    private List<Long> targetIds;

    /**
     * 兼容 query/form 的逗号分隔参数。
     */
    @Params(name = "targetIds", required = false)
    @Schema(description = "兼容 query/form 的逗号分隔目标用户ID字符串，例如 1,2,3")
    private String targetIdsText;
}
