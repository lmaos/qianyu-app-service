package com.clmcat.qianyu.mall.his.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "浏览历史列表查询")
public class BrowseHistoryQueryDTO {

    @Schema(description = "页码，默认 1")
    private Integer pageNum;

    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;
}
