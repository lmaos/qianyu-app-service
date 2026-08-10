package com.clmcat.qianyu.gift.api.prop;

import com.clmcat.qianyu.gift.api.prop.model.dto.PropItemDto;

import java.util.List;
import java.util.Map;

/**
 * 道具 RPC 接口。
 * <p>
 * 提供道具的查询、穿戴、卸下、使用等操作。
 *
 * @author ark-home
 * @date 2026-08-07
 */
public interface PropApi {

    /**
     * 道具背包列表。
     *
     * @param userId 用户ID
     * @return 道具列表
     */
    List<PropItemDto> getPropInventory(long userId);

    /**
     * 穿戴道具。
     * <p>
     * 同类型已穿戴的道具会被自动卸下，保证同类唯一穿戴。
     *
     * @param userId       用户ID
     * @param propRecordId 道具记录ID（user_prop_inventory.id）
     */
    void wearProp(long userId, long propRecordId);

    /**
     * 卸下道具。
     *
     * @param userId   用户ID
     * @param propType 道具类型（mount/frame/bubble/tag/title）
     */
    void removeProp(long userId, String propType);

    /**
     * 使用消耗型道具。
     *
     * @param userId       用户ID
     * @param propRecordId 道具记录ID
     */
    void useProp(long userId, long propRecordId);

    /**
     * 获取当前穿戴的道具。
     *
     * @param userId 用户ID
     * @return 按道具类型的穿戴映射：{"mount": PropItemDto, "frame": PropItemDto, ...}
     */
    Map<String, PropItemDto> getEquippedProps(long userId);
}
