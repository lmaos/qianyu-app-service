package com.clmcat.qianyu.mall.api.mch.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class MerchantDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String name;
    private Integer type;
    private String contactName;
    private String contactPhone;
    private String licenseNo;
    private String licenseImage;
    private String description;
    private String bankName;
    private String bankAccount;
    private String bankHolder;
    private String bankBranch;
    private String legalPersonName;
    private String legalPersonIdCard;
    private String contactEmail;
    private Integer settlementCycle;
    private Integer auditStatus;
    private String auditRemark;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}
