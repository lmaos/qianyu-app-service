package com.clmcat.qianyu.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "首页商城聚合数据")
public class HomePageVo {

    @Schema(description = "Tab 导航列表")
    private List<HomeTabVo> tabList;

    @Schema(description = "默认选中的 Tab key")
    private String defaultTabKey;

    @Schema(description = "Banner 轮播列表")
    private List<BannerVo> bannerList;

    @Schema(description = "区域/楼层列表")
    private List<ZoneVo> zoneList;
}
