package com.clmcat.qianyu.mall.fav.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "收藏操作结果")
public class FavActionResultVO {

    @Schema(description = "收藏记录 ID")
    private Long favId;

    @Schema(description = "最终收藏状态")
    private Boolean isFav;
}
