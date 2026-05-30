package com.clmcat.qianyu.mall.fav.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "批量取消收藏结果")
public class FavBatchCancelResultVO {

    @Schema(description = "成功取消的条数")
    private Integer count;
}
