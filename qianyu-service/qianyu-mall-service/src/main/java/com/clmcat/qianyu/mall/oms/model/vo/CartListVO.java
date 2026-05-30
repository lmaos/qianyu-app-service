package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "购物车列表")
public class CartListVO {

    @Schema(description = "购物车项列表")
    private List<CartItemVO> list;

    @Schema(description = "合计金额（元）")
    private String totalPrice;

    @Schema(description = "合计商品件数")
    private Integer totalCount;
}
