package com.clmcat.qianyu.mall.api.mch.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 运营端提现审批分页查询入参（06-api-contract.md §4.2）。
 * <p>platform 视角跨商家筛选，pageNum/pageSize 缺省 1/10。
 */
@Data
public class WithdrawalPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商家 ID（可空=全平台），运营可按单店筛选。 */
    private Long merchantId;

    /** 提现状态：0待审/1通过/2打款中/3成功/4拒绝/5失败。 */
    private Integer status;

    /** 提现单号模糊匹配。 */
    private String withdrawalNo;

    /** 起始申请时间（毫秒，含）。 */
    private Long createTimeStart;

    /** 截止申请时间（毫秒，含）。 */
    private Long createTimeEnd;

    /** 页码，缺省 1。 */
    private Integer pageNum;

    /** 每页条数，缺省 10。 */
    private Integer pageSize;
}
