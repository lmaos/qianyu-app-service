package com.clmcat.qianyu.mall.his.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "删除浏览历史请求")
public class BrowseHistoryDeleteDTO {

    @Schema(description = "待删除记录 ID 列表")
    private List<Long> ids;

    @Schema(description = "是否清空全部历史，默认 false。为 true 时忽略 ids")
    private Boolean clearAll;
}
