package com.clmcat.qianyu.gift.rule;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 权重随机选择器（盲盒掉落）。
 *
 * @author ark-home
 * @date 2026-08-07
 */
public class WeightedRandomPicker {

    /**
     * 按权重随机选择一个索引。
     *
     * @param weights 权重列表
     * @return 选中的索引，如果总权重<=0 返回 0
     */
    public static int pick(List<Integer> weights) {
        int totalWeight = 0;
        for (int w : weights) {
            totalWeight += w;
        }
        if (totalWeight <= 0) {
            return 0;
        }
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < weights.size(); i++) {
            cumulative += weights.get(i);
            if (random < cumulative) {
                return i;
            }
        }
        return weights.size() - 1;
    }
}
