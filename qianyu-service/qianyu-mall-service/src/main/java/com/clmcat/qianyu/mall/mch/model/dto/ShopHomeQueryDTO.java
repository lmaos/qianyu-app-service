package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "店铺首页查询请求")
public class ShopHomeQueryDTO {

    @Schema(description = "商家 ID")
    private Long merchantId;
}
