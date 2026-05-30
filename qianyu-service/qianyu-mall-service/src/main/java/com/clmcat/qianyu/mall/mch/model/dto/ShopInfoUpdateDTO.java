package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "店铺信息更新请求")
public class ShopInfoUpdateDTO {

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "Logo URL")
    private String shopLogo;

    @Schema(description = "Banner URL")
    private String shopBanner;

    @Schema(description = "店铺简介")
    private String description;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "电话")
    private String contactPhone;

    @Schema(description = "邮箱")
    private String contactEmail;
}
