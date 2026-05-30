package com.clmcat.qianyu.mall.fav.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "收藏状态")
public class FavStatusVO {

    @Schema(description = "是否已收藏")
    private Boolean isFav;

    @Schema(description = "收藏记录 ID（未收藏时为 0）")
    private Long favId;
}
