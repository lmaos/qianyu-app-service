package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "创建 SPU 请求")
public class SpuCreateDto {

    @Schema(description = "商品名称，最长 200 字符")
    private String name;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "品牌 ID")
    private Long brandId;

    @Schema(description = "主图 URL")
    private String mainImage;

    @Schema(description = "轮播图，至少 1 张")
    private List<String> images;

    @Schema(description = "富文本描述（含详情图）")
    private String description;

    @Schema(description = "搜索关键词（逗号分隔）")
    private String keywords;

    @Schema(description = "计量单位（默认\"个\"）")
    private String unit;

    @Schema(description = "运费模板 ID，不传则免运费")
    private Long freightTemplateId;

    @Schema(description = "SKU 列表，至少 1 个")
    private List<SkuCreateItem> skuList;

    @Data
    @Schema(description = "SKU 创建项")
    public static class SkuCreateItem {

        @Schema(description = "SKU 名称，如 \"红色-XL\"")
        private String skuName;

        @Schema(description = "规格值拼接，如 \"红色,XL\"")
        private String specs;

        @Schema(description = "售价（元），大于 0")
        private String price;

        @Schema(description = "原价（元）")
        private String originalPrice;

        @Schema(description = "初始库存，大于等于 0")
        private Integer stock;

        @Schema(description = "SKU 图片 URL")
        private String image;

        @Schema(description = "是否默认 SKU，不传则第一个为默认")
        private Boolean isDefault;

        @Schema(description = "重量（kg）")
        private BigDecimal weight;

        @Schema(description = "体积（m³）")
        private BigDecimal volume;
    }
}
