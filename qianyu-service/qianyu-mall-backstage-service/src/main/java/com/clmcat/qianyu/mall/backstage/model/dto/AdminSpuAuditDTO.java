package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

@Data
public class AdminSpuAuditDTO {
    private Long spuId;
    private Boolean approved;
    private String rejectReason;
}
