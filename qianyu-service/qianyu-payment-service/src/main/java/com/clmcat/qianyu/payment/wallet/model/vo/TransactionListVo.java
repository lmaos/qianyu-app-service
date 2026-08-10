package com.clmcat.qianyu.payment.wallet.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 交易流水列表视图对象（返回前端），含游标分页。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListVo {

    /** 空列表单例 */
    public static final TransactionListVo EMPTY = TransactionListVo.builder()
            .transactions(Collections.emptyList())
            .cursor(0L)
            .hasMore(false)
            .build();

    /** 交易流水列表 */
    private List<TransactionVo> transactions;

    /** 下一页游标 */
    private Long cursor;

    /** 是否还有更多 */
    private Boolean hasMore;
}
