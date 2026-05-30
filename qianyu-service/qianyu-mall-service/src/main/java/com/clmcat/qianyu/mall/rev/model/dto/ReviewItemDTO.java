package com.clmcat.qianyu.mall.rev.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "评价项")
public class ReviewItemDTO {

    @Schema(description = "订单商品项 ID")
    private Long orderItemId;

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "商品评分 1-5 分")
    private Integer score;

    @Schema(description = "评价内容，最长 500 字符")
    private String content;

    @Schema(description = "评价图片，最多 9 张")
    private List<String> images;

    @Schema(description = "是否匿名评价")
    private Boolean anonymous;
}
