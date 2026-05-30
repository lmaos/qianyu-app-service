package com.clmcat.qianyu.mall.his.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "搜索热词查询请求")
public class SearchHotQueryDTO {

    @Schema(description = "返回数量，默认 10，最大 30")
    private Integer limit;
}
