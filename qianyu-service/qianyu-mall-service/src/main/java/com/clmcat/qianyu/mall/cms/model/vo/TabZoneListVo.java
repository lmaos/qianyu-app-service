package com.clmcat.qianyu.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 单个 Tab 下的 Zone 楼层列表（不含 banner / tabList）
 *
 * <p>独立于 {@link HomePageVo}，避免在不需要 banner/tab 时把它们也吐回去
 */
@Getter
@Builder
@Schema(description = "Tab 下的 Zone 列表")
public class TabZoneListVo {

    @Schema(description = "区域/楼层列表（结构与 homePage.zoneList 一致）")
    private List<ZoneVo> zoneList;
}
