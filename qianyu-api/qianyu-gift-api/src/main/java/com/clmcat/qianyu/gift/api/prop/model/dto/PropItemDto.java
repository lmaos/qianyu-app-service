package com.clmcat.qianyu.gift.api.prop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 道具项 DTO。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 道具记录ID（user_prop_inventory.id） */
    private Long id;

    /** 道具配置ID */
    private Long propId;

    /** 道具名称 */
    private String propName;

    /** 道具图标 */
    private String propIcon;

    /** 道具类型 */
    private String propType;

    /** 道具行为：1=穿戴 2=消耗 */
    private Integer usageType;

    /** 状态：0=背包中 1=穿戴中 2=已使用 3=已过期 */
    private Integer status;

    /** 过期时间戳（毫秒），0=永久 */
    private Long expireTime;

    /** 获取时间戳（毫秒） */
    private Long obtainTime;

    /** 来源类型 */
    private String sourceType;
}
