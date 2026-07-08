package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/** 运营拒绝提现请求（/api/admin/withdrawal/reject，0/1/2→4）。 */
@Data
public class AdminWithdrawalRejectDTO {
    private Long withdrawalId;
    private String rejectReason;
}
