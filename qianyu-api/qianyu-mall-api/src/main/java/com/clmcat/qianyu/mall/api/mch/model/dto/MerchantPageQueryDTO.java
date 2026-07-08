package com.clmcat.qianyu.mall.api.mch.model.dto;

import lombok.Data;
import java.io.Serializable;

/** 运营端商户分页查询（06-api-contract）。 */
@Data
public class MerchantPageQueryDTO implements Serializable {
    private Integer auditStatus;   // 0待审/1通过/2拒绝
    private Integer status;        // 0禁用/1正常/2冻结
    private String keyword;        // 名称模糊
    private Integer pageNum;
    private Integer pageSize;
}
