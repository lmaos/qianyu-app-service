package com.clmcat.qianyu.mall.his.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "记录浏览请求")
public class BrowseRecordDTO {

    @Schema(description = "商品 SPU ID")
    private Long spuId;
}
