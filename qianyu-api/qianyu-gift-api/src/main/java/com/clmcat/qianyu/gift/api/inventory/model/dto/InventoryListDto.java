package com.clmcat.qianyu.gift.api.inventory.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 背包礼物列表 DTO。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@NoArgsConstructor
public class InventoryListDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 背包礼物列表 */
    private List<InventoryItemDto> items;

    /** 下一页游标 */
    private Long cursor;

    /** 是否还有更多 */
    private Boolean hasMore;

    public InventoryListDto(List<InventoryItemDto> items, Long cursor, Boolean hasMore) {
        this.items = items;
        this.cursor = cursor;
        this.hasMore = hasMore;
    }

    public static InventoryListDto empty() {
        return new InventoryListDto(Collections.emptyList(), 0L, false);
    }
}
