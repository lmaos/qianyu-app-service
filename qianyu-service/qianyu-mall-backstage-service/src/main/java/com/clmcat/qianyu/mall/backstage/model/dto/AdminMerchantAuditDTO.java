package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/** 运营审核商家入驻请求（/api/admin/merchant/audit）。 */
@Data
public class AdminMerchantAuditDTO {
    private Long merchantId;
    private Boolean approved;
    private String rejectReason;
}
