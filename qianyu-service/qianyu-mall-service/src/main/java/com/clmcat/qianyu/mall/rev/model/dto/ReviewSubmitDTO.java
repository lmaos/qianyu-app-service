package com.clmcat.qianyu.mall.rev.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "提交评价请求")
public class ReviewSubmitDTO {

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "评价项列表（每个商品一条）")
    private List<ReviewItemDTO> items;
}
