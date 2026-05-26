package com.clmcat.qianyu.social.moment.model.dto;

import com.clmcat.framework.webmvc.anns.Params;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "动态ID批量查询参数")
public class MomentIdsDto {
    /**
     * 动态编号集合，适合 JSON 请求体。
     */
    @Schema(description = "动态ID列表，适合 JSON 数组传参")
    private List<Long> momentIds;
    /**
     * 兼容 query/form 的逗号分隔参数，如：1,2,3
     */
    @Params(name = "momentIds", required = false)
    @Schema(description = "兼容 query/form 的逗号分隔动态ID字符串，例如 1,2,3")
    private String momentIdsText;
}
