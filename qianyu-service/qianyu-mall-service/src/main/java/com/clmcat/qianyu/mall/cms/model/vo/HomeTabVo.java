package com.clmcat.qianyu.mall.cms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "首页 Tab 项")
public class HomeTabVo {

    @Schema(description = "Tab ID")
    private Long id;

    @Schema(description = "显示名称")
    private String name;

    @Schema(description = "唯一标识如 recommend/digital")
    private String tabKey;

    @Schema(description = "关联分类ID（recommend 时为 null）")
    private Long categoryId;

    @Schema(description = "是否默认选中")
    private Boolean isDefault;
}
