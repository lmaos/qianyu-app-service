package com.clmcat.qianyu.mall.fav.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "收藏列表查询请求")
public class FavListQueryDTO {

    @Schema(description = "收藏类型：0-全部, 1-商品, 2-店铺，默认 0")
    private Integer type;

    @Schema(description = "页码，默认 1")
    private Integer pageNum;

    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;
}
