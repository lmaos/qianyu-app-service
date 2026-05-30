package com.clmcat.qianyu.mall.fav.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "批量收藏状态结果")
public class FavBatchStatusResultVO {

    @Schema(description = "收藏状态列表")
    private List<FavBatchStatusItemVO> list;
}
