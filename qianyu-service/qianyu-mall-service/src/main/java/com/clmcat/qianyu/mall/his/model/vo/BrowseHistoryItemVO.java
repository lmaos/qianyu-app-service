package com.clmcat.qianyu.mall.his.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "浏览历史项")
public class BrowseHistoryItemVO {

    @Schema(description = "记录 ID")
    private Long id;

    @Schema(description = "商品 SPU ID")
    private Long spuId;

    @Schema(description = "商品名称快照")
    private String spuName;

    @Schema(description = "商品主图 URL 快照")
    private String spuImage;

    @Schema(description = "浏览时最低 SKU 价格（元）")
    private String price;

    @Schema(description = "当前实时最低价（元）")
    private String currentPrice;

    @Schema(description = "是否在售")
    private Boolean onShelf;

    @Schema(description = "浏览时间")
    private String browseTime;
}
