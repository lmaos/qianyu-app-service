package com.clmcat.qianyu.gift.gift.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftDto;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendResult;
import com.clmcat.qianyu.gift.gift.model.entity.GiftConfig;
import com.clmcat.qianyu.gift.gift.model.entity.GiftSendRecord;

/**
 * 礼物模块支持类。
 * <p>
 * 提供雪花ID、Entity↔Dto 转换等通用能力。
 *
 * @author ark-home
 * @date 2026-08-07
 */
public class GiftSupport {

    protected static final CustomSnowflake GIFT_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    protected static final int MAX_LIMIT = 50;

    /** 前缀 */
    private static final String TRANS_NO_PREFIX = "GN";

    /** 分配统一交易流水号 */
    protected String allocateTransNo() {
        return TRANS_NO_PREFIX + GIFT_ID_SNOWFLAKE.nextId();
    }

    // ===== DTO 转换 =====

    public static GiftDto toDto(GiftConfig entity) {
        if (entity == null) {
            return null;
        }
        return GiftDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .animationUrl(entity.getAnimationUrl())
                .price(entity.getPrice())
                .giftType(entity.getGiftType())
                .category(entity.getCategory())
                .extraConfig(entity.getExtraConfig())
                .shelfScenes(entity.getShelfScenes())
                .sortOrder(entity.getSortOrder())
                .status(entity.getStatus())
                .commissionRate(entity.getCommissionRate())
                .animationDuration(entity.getAnimationDuration())
                .svgaUrl(entity.getSvgaUrl())
                .build();
    }

    public static GiftSendResult toResult(GiftSendRecord record) {
        if (record == null) {
            return null;
        }
        return GiftSendResult.builder()
                .recordId(record.getId())
                .transNo(record.getTransNo())
                .bizNo(record.getBizNo())
                .giftId(record.getGiftId())
                .giftName(record.getGiftName())
                .actualGiftId(record.getActualGiftId())
                .actualGiftName(record.getActualGiftName())
                .totalAmount(record.getTotalAmount())
                .settleAmount(record.getSettleAmount())
                .status(record.getStatus())
                .build();
    }

    protected static String defaultEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
