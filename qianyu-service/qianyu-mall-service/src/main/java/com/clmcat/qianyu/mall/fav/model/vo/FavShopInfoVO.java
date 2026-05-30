package com.clmcat.qianyu.mall.fav.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "收藏店铺信息")
public class FavShopInfoVO {

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "Logo URL")
    private String shopLogo;

    @Schema(description = "累计销量")
    private Integer salesCount;

    @Schema(description = "在售商品数")
    private Integer spuCount;

    @Schema(description = "店铺评分")
    private BigDecimal score;
}
