package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "店铺首页聚合响应")
public class StoreHomeVO {

    // ========== 店铺基础信息 ==========

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "店铺 ID")
    private Long storeId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺 Logo URL")
    private String shopLogo;

    @Schema(description = "店铺 Banner 图 URL")
    private String shopBanner;

    @Schema(description = "店铺描述")
    private String description;

    // ========== 统计数据 ==========

    @Schema(description = "在售商品数")
    private Integer spuCount;

    @Schema(description = "总销量")
    private Integer salesCount;

    @Schema(description = "店铺评分 1.0~5.0")
    private BigDecimal score;

    @Schema(description = "评分展示文本，如 \"4.9\"")
    private String scoreText;

    @Schema(description = "在售商品数展示文本，如 \"80+\"")
    private String goodsCountText;

    // ========== 商品列表 ==========

    @Schema(description = "热销商品 TOP N")
    private List<SpuSimpleVO> hotProducts;

    @Schema(description = "最新商品 TOP N")
    private List<SpuSimpleVO> newProducts;

    // ========== P2 占位 ==========

    @Schema(description = "关注数文本，如 \"12.6万\"（P2）")
    private String followerText;

    @Schema(description = "客服回复率文本，如 \"98%\"（P2）")
    private String replyRateText;
}
