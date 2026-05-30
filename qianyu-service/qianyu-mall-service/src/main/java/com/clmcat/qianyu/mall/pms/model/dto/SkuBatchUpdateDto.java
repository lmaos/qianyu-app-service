package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "SKU 批量更新请求")
public class SkuBatchUpdateDto {

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "需更新的 SKU 列表")
    private List<SkuUpdateItem> items;

    @Data
    @Schema(description = "SKU 更新项")
    public static class SkuUpdateItem {

        @Schema(description = "SKU ID")
        private Long skuId;

        @Schema(description = "新 SKU 名称")
        private String skuName;

        @Schema(description = "新售价（元）")
        private String price;

        @Schema(description = "新原价（元）")
        private String originalPrice;

        @Schema(description = "新库存（绝对值）")
        private Integer stock;

        @Schema(description = "新 SKU 图片 URL")
        private String image;

        @Schema(description = "设为默认 SKU（每个 SPU 最多一个）")
        private Boolean isDefault;

        @Schema(description = "重量（kg）")
        private BigDecimal weight;

        @Schema(description = "体积（m³）")
        private BigDecimal volume;
    }
}
