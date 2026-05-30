package com.clmcat.qianyu.mall.fav.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "批量收藏状态项")
public class FavBatchStatusItemVO {

    @Schema(description = "目标 ID")
    private Long targetId;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "是否已收藏")
    private Boolean isFav;
}
