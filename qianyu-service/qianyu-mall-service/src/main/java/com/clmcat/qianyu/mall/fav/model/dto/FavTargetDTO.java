package com.clmcat.qianyu.mall.fav.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "收藏目标请求")
public class FavTargetDTO {

    @Schema(description = "收藏目标 ID（商品 SPU ID 或商家 ID）")
    private Long targetId;

    @Schema(description = "收藏类型：1-商品, 2-店铺")
    private Integer type;
}
