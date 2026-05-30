package com.clmcat.qianyu.mall.cms.model.vo;

import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "首页区域/楼层")
public class ZoneVo {

    @Schema(description = "区域 ID")
    private Long id;

    @Schema(description = "区域标题")
    private String title;

    @Schema(description = "标签文案")
    private String tagText;

    @Schema(description = "更多按钮文案")
    private String moreText;

    @Schema(description = "布局模式: double/quad-card")
    private String layoutMode;

    @Schema(description = "区域背景 CSS")
    private String surfaceBackground;

    @Schema(description = "区域阴影 CSS")
    private String surfaceShadow;

    @Schema(description = "商品列表")
    private List<SpuSimpleVo> productList;
}
