package com.clmcat.qianyu.mall.api.mch.model.dto;

import lombok.Data;
import java.io.Serializable;

import java.math.BigDecimal;

@Data
public class MerchantAccountDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long merchantId;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalWithdraw;
    private BigDecimal totalRefund;
    private BigDecimal totalCommission;
    private Long version;
    private Long createTime;
    private Long updateTime;
}
