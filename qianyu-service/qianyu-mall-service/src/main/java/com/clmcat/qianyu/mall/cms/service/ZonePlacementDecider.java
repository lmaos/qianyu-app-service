package com.clmcat.qianyu.mall.cms.service;

import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;

import java.util.List;

/**
 * 楼层自动投放决策器。由 {@code CmsZoneAutoFillTask} 在 SPU 上架时调用，
 * 决定该商品自动进入哪些楼层（及初始排序）。
 *
 * <p>本接口为「写接口不写实现」的扩展点：默认实现 {@code NoopZonePlacementDecider} 返回空（不进入），
 * 未来接入规则表/运营策略时替换实现即可，状态机与任务不变。
 */
public interface ZonePlacementDecider {

    /**
     * @param spu 刚上架的商品（status=1）
     * @return 命中的楼层投放指令；空列表表示不进入任何楼层
     */
    List<Placement> decide(PmsSpu spu);

    /** 单条投放指令：进哪个楼层、初始排序。 */
    record Placement(Long zoneId, Integer sort) {
    }
}
