package com.clmcat.qianyu.gift.inventory.service;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.gift.api.prop.PropApi;
import com.clmcat.qianyu.gift.api.prop.model.dto.PropItemDto;
import com.clmcat.qianyu.gift.gift.support.GiftSupport;
import com.clmcat.qianyu.gift.inventory.mapper.PropConfigMapper;
import com.clmcat.qianyu.gift.inventory.mapper.UserPropInventoryMapper;
import com.clmcat.qianyu.gift.inventory.model.entity.PropConfig;
import com.clmcat.qianyu.gift.inventory.model.entity.UserPropInventory;
import com.clmcat.qianyu.gift.model.entity.status.GiftStatus;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 道具服务。
 * <p>
 * 实现 {@link PropApi}，提供道具的查询、穿戴、卸下、使用等操作。
 * 同类型穿戴互斥通过事务中先卸下同类型再穿上当前道具来保证。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@DubboService
@Service
public class PropServiceBiz extends GiftSupport implements PropApi {

    @Resource
    private UserPropInventoryMapper userPropInventoryMapper;

    @Resource
    private PropConfigMapper propConfigMapper;

    @Override
    public List<PropItemDto> getPropInventory(long userId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        long now = System.currentTimeMillis();
        List<UserPropInventory> props = userPropInventoryMapper.customSelectByUserId(userId, now);
        return toDtoList(props);
    }

    @Override
    @Transactional
    public void wearProp(long userId, long propRecordId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(propRecordId <= 0);

        long now = System.currentTimeMillis();

        // ① 查询道具记录
        UserPropInventory prop = userPropInventoryMapper.customSelectById(propRecordId);
        GiftStatus.PROP_NOT_FOUND.assertThrowResEx(prop == null);
        GiftStatus.PROP_NOT_FOUND.assertThrowResEx(!prop.getUserId().equals(userId), "道具不属于该用户");

        // ② 校验状态
        if (prop.getStatus() == UserPropInventory.STATUS_EXPIRED
                || (prop.getExpireTime() > 0 && prop.getExpireTime() < now)) {
            GiftStatus.PROP_EXPIRED.assertThrowResEx(true);
        }
        GiftStatus.PROP_NOT_WEARABLE.assertThrowResEx(
                prop.getStatus() != UserPropInventory.STATUS_IN_BACKPACK
                        && prop.getStatus() != UserPropInventory.STATUS_EQUIPPED,
                "道具不可穿戴");

        // ③ 校验道具配置
        PropConfig config = propConfigMapper.customSelectById(prop.getPropId());
        GiftStatus.PROP_NOT_FOUND.assertThrowResEx(config == null);
        GiftStatus.PROP_NOT_WEARABLE.assertThrowResEx(
                config.getUsageType() != PropConfig.USAGE_TYPE_WEAR, "该道具不可穿戴");

        // ④ 同类型已穿戴的 → 卸下
        userPropInventoryMapper.customUnequipByType(userId, prop.getPropType(), now);

        // ⑤ 当前道具 → 穿上
        int affected = userPropInventoryMapper.customUpdateStatus(propRecordId, userId,
                UserPropInventory.STATUS_EQUIPPED, now);
        GiftStatus.PROP_NOT_IN_BACKPACK.assertThrowResEx(affected == 0);
    }

    @Override
    @Transactional
    public void removeProp(long userId, String propType) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(propType == null || propType.trim().isEmpty(), "道具类型不能为空");

        long now = System.currentTimeMillis();
        userPropInventoryMapper.customUnequipByType(userId, propType.trim(), now);
    }

    @Override
    @Transactional
    public void useProp(long userId, long propRecordId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(propRecordId <= 0);

        long now = System.currentTimeMillis();

        UserPropInventory prop = userPropInventoryMapper.customSelectById(propRecordId);
        GiftStatus.PROP_NOT_FOUND.assertThrowResEx(prop == null);
        GiftStatus.PROP_NOT_FOUND.assertThrowResEx(!prop.getUserId().equals(userId), "道具不属于该用户");
        GiftStatus.PROP_EXPIRED.assertThrowResEx(
                prop.getExpireTime() > 0 && prop.getExpireTime() < now);
        GiftStatus.PROP_NOT_FOUND.assertThrowResEx(
                prop.getStatus() != UserPropInventory.STATUS_IN_BACKPACK, "道具不可使用");

        int affected = userPropInventoryMapper.customUpdateStatus(propRecordId, userId,
                UserPropInventory.STATUS_USED, now);
        GiftStatus.PROP_NOT_IN_BACKPACK.assertThrowResEx(affected == 0);
    }

    @Override
    public Map<String, PropItemDto> getEquippedProps(long userId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);

        long now = System.currentTimeMillis();
        List<UserPropInventory> equipped = userPropInventoryMapper.customSelectAllEquipped(userId, now);
        Map<String, PropItemDto> result = new LinkedHashMap<>();
        for (UserPropInventory prop : equipped) {
            result.put(prop.getPropType(), toDto(prop));
        }
        return result;
    }

    // ---- 私有方法 ----

    private List<PropItemDto> toDtoList(List<UserPropInventory> props) {
        List<PropItemDto> result = new ArrayList<>(props.size());
        for (UserPropInventory prop : props) {
            result.add(toDto(prop));
        }
        return result;
    }

    private PropItemDto toDto(UserPropInventory prop) {
        PropConfig config = propConfigMapper.customSelectById(prop.getPropId());
        return PropItemDto.builder()
                .id(prop.getId())
                .propId(prop.getPropId())
                .propName(config != null ? config.getName() : "未知道具")
                .propIcon(config != null ? config.getIcon() : "")
                .propType(prop.getPropType())
                .usageType(config != null ? config.getUsageType() : PropConfig.USAGE_TYPE_WEAR)
                .status(prop.getStatus())
                .expireTime(prop.getExpireTime())
                .obtainTime(prop.getObtainTime())
                .sourceType(prop.getSourceType())
                .build();
    }
}
