package com.clmcat.qianyu.gift.rule;

import com.clmcat.qianyu.gift.gift.model.entity.GiftConfig;
import org.springframework.stereotype.Component;

/**
 * 普通礼物处理器。
 * <p>
 * 直接按 price × quantity 计价，按默认比例分佣，无特殊逻辑。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Component
public class NormalGiftHandler implements GiftTypeHandler {

    @Override
    public int giftType() {
        return GiftConfig.TYPE_NORMAL;
    }

    @Override
    public long resolvePrice(GiftConfig gift, int quantity) {
        return gift.getPrice() * quantity;
    }
}
