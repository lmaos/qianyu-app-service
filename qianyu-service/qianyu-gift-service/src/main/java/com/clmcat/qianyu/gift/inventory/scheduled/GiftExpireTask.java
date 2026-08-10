package com.clmcat.qianyu.gift.inventory.scheduled;

import com.clmcat.qianyu.gift.inventory.mapper.UserGiftInventoryMapper;
import com.clmcat.qianyu.gift.inventory.mapper.UserPropInventoryMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 礼物/道具过期处理定时任务。
 * <p>
 * 每 5 分钟执行一次，批量处理已过期的库存。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Component
public class GiftExpireTask {

    private static final Logger log = LoggerFactory.getLogger(GiftExpireTask.class);

    private static final int BATCH_LIMIT = 200;

    @Resource
    private UserGiftInventoryMapper userGiftInventoryMapper;

    @Resource
    private UserPropInventoryMapper userPropInventoryMapper;

    @Scheduled(fixedDelay = 300_000)
    public void expire() {
        long now = System.currentTimeMillis();
        log.debug("GiftExpireTask start, now={}", now);

        try {
            int giftExpired = userGiftInventoryMapper.customMarkExpired(now, BATCH_LIMIT);
            if (giftExpired > 0) {
                log.info("Marked {} gift inventory rows as expired", giftExpired);
            }

            int usedUp = userGiftInventoryMapper.customMarkUsedUp(now);
            if (usedUp > 0) {
                log.info("Marked {} gift inventory rows as used up", usedUp);
            }

            int propExpired = userPropInventoryMapper.customMarkExpired(now, BATCH_LIMIT);
            if (propExpired > 0) {
                log.info("Marked {} prop rows as expired", propExpired);
            }
        } catch (Exception e) {
            log.error("GiftExpireTask error", e);
        }

        log.debug("GiftExpireTask end");
    }
}
