package com.clmcat.qianyu.mall.rev.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "评价项")
public class ReviewItemVO {

    @Schema(description = "评价 ID")
    private Long id;

    @Schema(description = "评价用户 ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNick;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "规格描述")
    private String skuSpecs;

    @Schema(description = "评分 1-5")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "评价图片列表")
    private List<String> images;

    @Schema(description = "商家回复")
    private MerchantReplyVO merchantReply;

    @Schema(description = "评价时间")
    private String createTime;

    @Schema(description = "是否匿名")
    private Boolean anonymous;

    // ========== P2: 用户等级展示（暂无逻辑实现，字段值为 null） ==========

    @Schema(description = "用户会员等级，如\"会员 Lv.3\"")
    private String memberLevel;
}
