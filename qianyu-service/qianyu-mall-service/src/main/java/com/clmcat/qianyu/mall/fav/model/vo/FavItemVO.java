package com.clmcat.qianyu.mall.fav.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "收藏列表项")
public class FavItemVO {

    @Schema(description = "收藏记录 ID")
    private Long id;

    @Schema(description = "收藏目标 ID")
    private Long targetId;

    @Schema(description = "收藏类型")
    private Integer type;

    @Schema(description = "类型中文（\"商品\" / \"店铺\"）")
    private String typeText;

    @Schema(description = "商品信息（type=1 时有值）")
    private FavSpuInfoVO spuInfo;

    @Schema(description = "店铺信息（type=2 时有值）")
    private FavShopInfoVO shopInfo;

    @Schema(description = "收藏时间")
    private String createTime;
}
