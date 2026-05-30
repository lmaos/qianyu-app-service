package com.clmcat.qianyu.mall.his.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "记录搜索关键词请求")
public class SearchKeywordRecordDTO {

    @Schema(description = "搜索关键词，最长 128 字符")
    private String keyword;
}
