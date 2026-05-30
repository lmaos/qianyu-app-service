package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "店铺信息摘要（商品详情页内嵌）")
public class ShopSimpleVo {

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "店铺 ID")
    private Long storeId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺 Logo URL")
    private String shopLogo;

    @Schema(description = "店铺描述")
    private String description;
}
