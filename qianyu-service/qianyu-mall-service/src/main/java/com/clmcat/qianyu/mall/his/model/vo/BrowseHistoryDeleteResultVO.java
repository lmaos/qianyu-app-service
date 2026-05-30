package com.clmcat.qianyu.mall.his.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "删除浏览历史结果")
public class BrowseHistoryDeleteResultVO {

    @Schema(description = "成功删除的条数")
    private Integer count;
}
