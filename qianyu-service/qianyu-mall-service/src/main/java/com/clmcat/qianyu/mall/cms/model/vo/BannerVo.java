package com.clmcat.qianyu.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Banner 项")
public class BannerVo {

    @Schema(description = "Banner ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述文案")
    private String desc;

    @Schema(description = "按钮文案")
    private String actionText;

    @Schema(description = "标签文案")
    private String tagText;

    @Schema(description = "图片 URL")
    private String image;

    @Schema(description = "跳转类型: 0无 1SPU详情 2分类页 3外链")
    private Integer linkType;

    @Schema(description = "跳转目标值")
    private String linkValue;
}
