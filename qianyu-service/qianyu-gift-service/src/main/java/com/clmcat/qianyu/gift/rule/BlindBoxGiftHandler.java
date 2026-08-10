package com.clmcat.qianyu.gift.rule;

import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendRequest;
import com.clmcat.qianyu.gift.gift.mapper.GiftBlindboxDropMapper;
import com.clmcat.qianyu.gift.gift.mapper.GiftConfigMapper;
import com.clmcat.qianyu.gift.gift.model.entity.GiftBlindboxDrop;
import com.clmcat.qianyu.gift.gift.model.entity.GiftConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 盲盒礼物处理器。
 * <p>
 * 价格固定为盲盒本身价格（quantity 恒为 1），随机开出掉落池中的一个礼物，
 * 主播分成按开出的礼物价值计算。结算采用两阶段：先扣款（settleAmount=0），后结算开出的礼物价值。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Component
public class BlindBoxGiftHandler implements GiftTypeHandler {

    @Resource
    private GiftBlindboxDropMapper giftBlindboxDropMapper;

    @Resource
    private GiftConfigMapper giftConfigMapper;

    @Override
    public int giftType() {
        return GiftConfig.TYPE_BLINDBOX;
    }

    @Override
    public long resolvePrice(GiftConfig gift, int quantity) {
        // 盲盒按单个价格，quantity 恒为 1
        return gift.getPrice();
    }

    @Override
    public OpenResult open(GiftConfig gift, GiftSendRequest req) {
        List<GiftBlindboxDrop> drops = giftBlindboxDropMapper.customSelectByBlindboxId(gift.getId());
        if (drops == null || drops.isEmpty()) {
            // 无掉落配置时返回自身
            return new OpenResult(gift.getId(), gift.getName(), gift.getPrice());
        }
        List<Integer> weights = new ArrayList<>(drops.size());
        for (GiftBlindboxDrop drop : drops) {
            weights.add(drop.getWeight());
        }
        int index = WeightedRandomPicker.pick(weights);
        GiftBlindboxDrop selected = drops.get(index);

        // 查询开出的礼物信息
        GiftConfig openedGift = giftConfigMapper.customSelectById(selected.getDropGiftId());
        if (openedGift == null) {
            return new OpenResult(gift.getId(), gift.getName(), gift.getPrice());
        }
        return new OpenResult(openedGift.getId(), openedGift.getName(), openedGift.getPrice());
    }

    @Override
    public long resolveSettleAmount(GiftConfig gift, OpenResult opened, long totalPaid, int rate) {
        // 盲盒按开出的礼物价值结算
        if (opened != null && opened.actualPrice() > 0) {
            return opened.actualPrice() * (10000L - rate) / 10000L;
        }
        return totalPaid * (10000L - rate) / 10000L;
    }
}
