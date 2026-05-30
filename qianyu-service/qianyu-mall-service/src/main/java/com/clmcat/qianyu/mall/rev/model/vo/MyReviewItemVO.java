package com.clmcat.qianyu.mall.rev.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "我的评价项")
public class MyReviewItemVO {

    @Schema(description = "评价 ID")
    private Long id;

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "商品主图")
    private String spuImage;

    @Schema(description = "规格描述")
    private String skuSpecs;

    @Schema(description = "评分")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "评价图片")
    private List<String> images;

    @Schema(description = "商家回复")
    private MerchantReplyVO merchantReply;

    @Schema(description = "评价时间")
    private String createTime;
}
