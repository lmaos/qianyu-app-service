package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 单商户 ID 请求体（运营查询指定商户的账单/账户/提现等）。
 * <p>用于运营端 POST body：{@code {merchantId}}。
 */
@Data
public class AdminMerchantIdDTO {

    /** 商户 ID（雪花 ID，必填）。 */
    private Long merchantId;
}
