package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "店铺信息")
public class ShopInfoVO {

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "Logo")
    private String shopLogo;

    @Schema(description = "Banner")
    private String shopBanner;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "电话")
    private String contactPhone;

    @Schema(description = "邮箱")
    private String contactEmail;

    @Schema(description = "商家状态：1-正常, 2-冻结")
    private Integer status;

    @Schema(description = "入驻时间")
    private String createTime;

    @Schema(description = "店铺评分")
    private BigDecimal score;

    @Schema(description = "累计销量")
    private Integer salesCount;

    @Schema(description = "在售商品数")
    private Integer spuCount;
}
