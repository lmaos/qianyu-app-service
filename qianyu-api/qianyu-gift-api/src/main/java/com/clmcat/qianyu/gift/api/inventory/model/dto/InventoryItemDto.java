package com.clmcat.qianyu.gift.api.inventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 背包礼物项 DTO。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 库存记录ID */
    private Long id;

    /** 礼物ID */
    private Long giftId;

    /** 礼物名称 */
    private String giftName;

    /** 礼物图标 */
    private String giftIcon;

    /** 持有数量 */
    private Integer quantity;

    /** 过期时间戳（毫秒），0=永久 */
    private Long expireTime;

    /** 来源类型 */
    private String sourceType;
}
