package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@Schema(description = "SKU 项")
public class SkuItemVo {

    @Schema(description = "SKU ID")
    private Long id;

    @Schema(description = "SKU 名称，如 \"红色-XL\"")
    private String skuName;

    @Schema(description = "规格值拼接，如 \"红色,XL\"")
    private String specs;

    @Schema(description = "售价（元）")
    private String price;

    @Schema(description = "原价（元）")
    private String originalPrice;

    @Schema(description = "当前库存")
    private Integer stock;

    @Schema(description = "SKU 图片 URL（NULL 时回退到 SPU main_image）")
    private String image;

    @Schema(description = "是否默认 SKU")
    private Boolean isDefault;

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "重量（kg）")
    private BigDecimal weight;

    @Schema(description = "体积（m³）")
    private BigDecimal volume;

    // ========== P2: 营销/物流/媒体标签（暂无逻辑实现，字段值为 null） ==========

    @Schema(description = "促销文案，如\"限时 9 折\"")
    private String promotionText;

    @Schema(description = "服务标签列表，如[\"7天保价\",\"支持发票\",\"24小时发货\"]")
    private List<String> authTagList;

    @Schema(description = "售后标签列表，如[\"运费险\",\"坏损包退\",\"只换不修\"]")
    private List<String> afterSaleTagList;

    @Schema(description = "补贴文案，如\"国补立减 30 元\"")
    private String subsidyText;

    @Schema(description = "折扣文案，如\"限时 9 折\"")
    private String discountText;

    @Schema(description = "满减文案，如\"满 300 减 30\"")
    private String fullReductionText;

    @Schema(description = "物流信息，{shipTime:\"24小时发货\", logisticName:\"顺丰速运\"}")
    private Map<String, String> logisticsInfo;

    @Schema(description = "媒体列表，[{type:\"image\"|\"video\", url:\"...\", poster:\"...\"}]")
    private List<Map<String, String>> mediaList;
}
