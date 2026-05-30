package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "店铺首页信息")
public class ShopHomeVO {

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺 Logo URL")
    private String shopLogo;

    @Schema(description = "店铺 Banner 图 URL")
    private String shopBanner;

    @Schema(description = "店铺简介")
    private String description;

    @Schema(description = "全店累计销量")
    private Integer salesCount;

    @Schema(description = "在售商品数")
    private Integer spuCount;

    @Schema(description = "店铺评分")
    private BigDecimal score;

    @Schema(description = "描述评分")
    private String scoreDesc;

    @Schema(description = "服务评分")
    private String scoreService;

    @Schema(description = "物流评分")
    private String scoreLogistics;

    @Schema(description = "热销商品（前 6 个）")
    private List<SpuSimpleVO> hotProducts;

    @Schema(description = "最新上架商品（前 6 个）")
    private List<SpuSimpleVO> newProducts;

    // ========== P2: 店铺统计展示（暂无逻辑实现，字段值为 null） ==========

    @Schema(description = "粉丝数文案，如\"12.6万\"")
    private String followerText;

    @Schema(description = "客服回复率文案，如\"98%\"")
    private String replyRateText;
}
