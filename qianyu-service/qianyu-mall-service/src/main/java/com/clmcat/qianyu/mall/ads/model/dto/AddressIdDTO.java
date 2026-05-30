package com.clmcat.qianyu.mall.ads.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "地址 ID 请求")
public class AddressIdDTO {

    @Schema(description = "地址 ID")
    private Long addressId;
}
