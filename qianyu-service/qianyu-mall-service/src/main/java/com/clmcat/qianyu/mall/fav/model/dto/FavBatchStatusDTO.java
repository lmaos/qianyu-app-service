package com.clmcat.qianyu.mall.fav.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量收藏状态查询请求")
public class FavBatchStatusDTO {

    @Schema(description = "目标列表，最多 50 个")
    private List<FavTargetDTO> targets;
}
