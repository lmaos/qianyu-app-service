package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "编辑 SPU 请求")
public class SpuUpdateDto {

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "品牌 ID")
    private Long brandId;

    @Schema(description = "主图 URL")
    private String mainImage;

    @Schema(description = "轮播图")
    private List<String> images;

    @Schema(description = "富文本描述")
    private String description;

    @Schema(description = "搜索关键词")
    private String keywords;

    @Schema(description = "计量单位")
    private String unit;

    @Schema(description = "运费模板 ID")
    private Long freightTemplateId;

    @Schema(description = "SKU 列表（全量替换）")
    private List<SpuCreateDto.SkuCreateItem> skuList;
}
