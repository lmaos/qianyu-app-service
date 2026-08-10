package com.clmcat.qianyu.payment.api.settlement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 结算流水列表（RPC 返回），含游标分页。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementRecordListDto implements Serializable {

    public static final SettlementRecordListDto EMPTY = SettlementRecordListDto.builder()
            .records(Collections.emptyList())
            .cursor(0L)
            .hasMore(false)
            .build();

    private List<SettlementRecordDto> records;
    private Long cursor;
    private Boolean hasMore;
}
