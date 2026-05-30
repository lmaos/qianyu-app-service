package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "SPU 列表项（搜索/列表共用）")
public class SpuSimpleVo {

    @Schema(description = "SPU ID")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "主图 URL（缩略图）")
    private String mainImage;

    @Schema(description = "最低价格（元）")
    private String price;

    @Schema(description = "原价（元）")
    private String originalPrice;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "评价数")
    private Integer commentCount;

    @Schema(description = "平均评分")
    private BigDecimal avgScore;

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "店铺 ID")
    private Long storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    // ========== P2: 营销标签（暂无逻辑实现，字段值为 null） ==========

    @Schema(description = "紧凑角标文案，如\"国补\"\"补贴\"，在商品卡片上显示")
    private String compactBadge;
}
