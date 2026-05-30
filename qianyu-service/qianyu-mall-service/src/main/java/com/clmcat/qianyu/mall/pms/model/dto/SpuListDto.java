package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "SPU 列表请求（按分类/商家）")
public class SpuListDto {

    @Schema(description = "分类 ID，不传则查全部分类")
    private Long categoryId;

    @Schema(description = "商家 ID，查指定店铺商品")
    private Long merchantId;

    @Schema(description = "页码，默认 1")
    private Integer pageNum;

    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;
}
