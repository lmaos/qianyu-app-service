package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/** 运营审批提现通过请求（/api/admin/withdrawal/approve，0→1）。 */
@Data
public class AdminWithdrawalApproveDTO {
    private Long withdrawalId;
}
