package com.clmcat.qianyu.mall.his.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "搜索热词")
public class HotKeywordVO {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "热度/搜索次数")
    private Integer heat;
}
