package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "SPU 详情")
public class SpuDetailVo {

    @Schema(description = "SPU ID")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "主图 URL")
    private String mainImage;

    @Schema(description = "轮播图 URL 列表")
    private List<String> images;

    @Schema(description = "商品描述（富文本/HTML）")
    private String description;

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "品牌 ID")
    private Long brandId;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "最低 SKU 价格（元）")
    private String price;

    @Schema(description = "原价（元）")
    private String originalPrice;

    @Schema(description = "累计销量")
    private Integer sales;

    @Schema(description = "累计评价数")
    private Integer commentCount;

    @Schema(description = "平均评分 1.0~5.0")
    private BigDecimal avgScore;

    @Schema(description = "计量单位")
    private String unit;

    @Schema(description = "搜索关键词")
    private String keywords;

    @Schema(description = "运费模板 ID，NULL 表示免运费")
    private Long freightTemplateId;

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "店铺 ID")
    private Long storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "SKU 列表")
    private List<SkuItemVo> skuList;

    @Schema(description = "规格组列表")
    private List<SpecGroupVo> specGroups;

    @Schema(description = "默认选中的 SKU ID")
    private Long defaultSkuId;

    @Schema(description = "评价统计摘要")
    private ReviewStatVo reviewStat;

    @Schema(description = "店铺信息摘要")
    private ShopSimpleVo shopInfo;

    // ========== P2: 商品详情展示（暂无逻辑实现，字段值为 null） ==========

    @Schema(description = "售后服务说明（Markdown 格式）")
    private String serviceMarkdown;
}
