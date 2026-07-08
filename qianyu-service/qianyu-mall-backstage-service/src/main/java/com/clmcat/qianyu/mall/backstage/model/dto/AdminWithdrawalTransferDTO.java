package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/** 运营标记打款结果请求（/api/admin/withdrawal/markTransferred，2→3成功/2→5失败）。 */
@Data
public class AdminWithdrawalTransferDTO {
    private Long withdrawalId;
    /** 打款流水号（全局唯一）。 */
    private String transferNo;
    /** true=打款成功 status=3；false=打款失败 status=5。 */
    private Boolean success;
}
