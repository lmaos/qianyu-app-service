package com.clmcat.qianyu.gift.inventory.service;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.gift.api.inventory.InventoryApi;
import com.clmcat.qianyu.gift.api.inventory.model.dto.InventoryItemDto;
import com.clmcat.qianyu.gift.api.inventory.model.dto.InventoryListDto;
import com.clmcat.qianyu.gift.gift.mapper.GiftConfigMapper;
import com.clmcat.qianyu.gift.gift.model.entity.GiftConfig;
import com.clmcat.qianyu.gift.gift.support.GiftSupport;
import com.clmcat.qianyu.gift.inventory.mapper.UserGiftInventoryMapper;
import com.clmcat.qianyu.gift.inventory.model.entity.UserGiftInventory;
import com.clmcat.qianyu.gift.model.entity.status.GiftStatus;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 背包库存服务。
 * <p>
 * 实现 {@link InventoryApi}，提供礼物库存的查询和发放能力。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@DubboService
@Service
public class InventoryServiceBiz extends GiftSupport implements InventoryApi {

    @Resource
    private UserGiftInventoryMapper userGiftInventoryMapper;

    @Resource
    private GiftConfigMapper giftConfigMapper;

    @Override
    public InventoryListDto getGiftInventory(long userId, long cursor, int limit) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);

        if (limit <= 0 || limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }

        long now = System.currentTimeMillis();
        List<UserGiftInventory> records = userGiftInventoryMapper.customSelectByUserId(userId, now, cursor, limit + 1);
        if (records == null || records.isEmpty()) {
            return InventoryListDto.empty();
        }

        boolean hasMore = records.size() > limit;
        if (hasMore) {
            records = records.subList(0, limit);
        }

        long nextCursor = hasMore && !records.isEmpty()
                ? records.get(records.size() - 1).getId()
                : 0L;

        List<InventoryItemDto> items = new ArrayList<>(records.size());
        for (UserGiftInventory record : records) {
            GiftConfig gift = giftConfigMapper.customSelectById(record.getGiftId());
            items.add(InventoryItemDto.builder()
                    .id(record.getId())
                    .giftId(record.getGiftId())
                    .giftName(gift != null ? gift.getName() : "未知礼物")
                    .giftIcon(gift != null ? gift.getIcon() : "")
                    .quantity(record.getQuantity())
                    .expireTime(record.getExpireTime())
                    .sourceType(record.getSourceType())
                    .build());
        }

        return new InventoryListDto(items, nextCursor, hasMore);
    }

    @Override
    @Transactional
    public void addGiftToInventory(long userId, long giftId, int quantity, long expireTime,
                                   String sourceType, String sourceId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(giftId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(quantity <= 0, "数量必须大于0");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(sourceType == null || sourceType.trim().isEmpty(), "来源类型不能为空");

        // 校验礼物存在
        GiftConfig gift = giftConfigMapper.customSelectById(giftId);
        GiftStatus.GIFT_NOT_FOUND.assertThrowResEx(gift == null);

        long now = System.currentTimeMillis();
        long id = GIFT_ID_SNOWFLAKE.nextId();

        UserGiftInventory record = UserGiftInventory.builder()
                .id(id)
                .userId(userId)
                .giftId(giftId)
                .quantity(quantity)
                .sourceType(sourceType.trim())
                .sourceId(defaultEmpty(sourceId))
                .expireTime(expireTime)
                .status(UserGiftInventory.STATUS_VALID)
                .createTime(now)
                .updateTime(now)
                .build();

        try {
            userGiftInventoryMapper.customUpsert(record);
        } catch (DuplicateKeyException e) {
            // 并发发放同一礼物+同一过期时间，重试 upsert
            userGiftInventoryMapper.customUpsert(record);
        }
    }
}
