package com.clmcat.qianyu.payment.wallet.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易流水查询参数。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@NoArgsConstructor
public class TransactionQueryDto {

    /** 用户ID */
    private Long userId;

    /** 游标（上一页最后一条的 createTime，首次传 0） */
    private Long cursor;

    /** 每页条数 */
    private Integer limit;
}
