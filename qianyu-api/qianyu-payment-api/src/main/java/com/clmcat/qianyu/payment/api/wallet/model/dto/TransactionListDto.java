package com.clmcat.qianyu.payment.api.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 交易流水列表（RPC 返回），含游标分页。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListDto implements Serializable {

    /** 空列表单例 */
    public static final TransactionListDto EMPTY = TransactionListDto.builder()
            .transactions(Collections.emptyList())
            .cursor(0L)
            .hasMore(false)
            .build();

    /** 交易流水列表 */
    private List<TransactionDto> transactions;

    /** 下一页游标（取最后一条的 createTime，0 表示没有更多） */
    private Long cursor;

    /** 是否还有更多 */
    private Boolean hasMore;
}
