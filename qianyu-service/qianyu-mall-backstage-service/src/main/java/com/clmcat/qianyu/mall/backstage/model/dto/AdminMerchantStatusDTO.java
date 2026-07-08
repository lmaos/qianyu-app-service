package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/** 商户状态管控请求（freeze/unfreeze/disable）。 */
@Data
public class AdminMerchantStatusDTO {
    private Long merchantId;
}
