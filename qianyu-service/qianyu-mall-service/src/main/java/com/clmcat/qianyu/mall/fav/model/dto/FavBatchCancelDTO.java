package com.clmcat.qianyu.mall.fav.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量取消收藏请求")
public class FavBatchCancelDTO {

    @Schema(description = "收藏记录 ID 列表")
    private List<Long> favIds;
}
