package com.clmcat.qianyu.mall.pms.model.vo;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "商家商品管理页聚合响应")
public class MerchantGoodsPageVO {

    @Schema(description = "统计卡片列表")
    private List<SummaryItem> summaryList;

    @Schema(description = "商品分页列表")
    private Page<GoodsItem> goodsList;

    @Getter
    @Builder
    @Schema(description = "统计卡片项")
    public static class SummaryItem {

        @Schema(description = "统计项 key")
        private String key;

        @Schema(description = "统计项标签")
        private String label;

        @Schema(description = "统计项值")
        private String value;
    }

    @Getter
    @Builder
    @Schema(description = "商家商品列表项")
    public static class GoodsItem {

        @Schema(description = "SPU ID")
        private Long id;

        @Schema(description = "商品名称")
        private String title;

        @Schema(description = "商品主图 URL")
        private String coverBackground;

        @Schema(description = "主图占位文字")
        private String coverText;

        @Schema(description = "最低价（元）")
        private String price;

        @Schema(description = "库存文本，如 \"18 件库存\"")
        private String stockText;

        @Schema(description = "状态文本：在售中/待上架/库存预警")
        private String statusText;

        @Schema(description = "销量")
        private Integer sales;
    }
}
