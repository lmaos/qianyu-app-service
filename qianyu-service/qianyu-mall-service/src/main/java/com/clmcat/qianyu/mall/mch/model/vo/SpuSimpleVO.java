package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "精简版商品信息")
public class SpuSimpleVO {

    @Schema(description = "SPU ID")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "主图 URL")
    private String mainImage;

    @Schema(description = "价格（元）")
    private String price;

    @Schema(description = "销量")
    private Integer sales;
}
