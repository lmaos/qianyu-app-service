package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商家入驻申请请求")
public class MerchantSettleDTO {

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺 Logo URL")
    private String shopLogo;

    @Schema(description = "店铺 Banner 图")
    private String shopBanner;

    @Schema(description = "店铺简介")
    private String description;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "营业执照图片 URL")
    private String businessLicense;

    @Schema(description = "营业执照编号")
    private String businessLicenseNo;

    @Schema(description = "法人姓名")
    private String legalPersonName;

    @Schema(description = "法人身份证号")
    private String legalPersonIdCard;

    @Schema(description = "银行账户名")
    private String bankAccountName;

    @Schema(description = "银行账号")
    private String bankAccountNo;

    @Schema(description = "开户行")
    private String bankName;

    @Schema(description = "开户支行")
    private String bankBranch;
}
