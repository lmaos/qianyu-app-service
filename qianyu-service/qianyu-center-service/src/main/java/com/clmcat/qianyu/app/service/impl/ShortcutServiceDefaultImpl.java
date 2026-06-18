package com.clmcat.qianyu.app.service.impl;

import com.clmcat.qianyu.app.api.model.dto.ShortcutDto;
import com.clmcat.qianyu.app.model.config.ShortcutConfig;
import com.clmcat.qianyu.app.service.ShortcutService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 快捷入口默认实现（写死）。
 * <p>
 * 当前仅返回本期上线的 4 个入口：主播中心、商家管理、订单中心、我的钱包。
 * 后续接入各子服务动态查询时替换此实现即可。
 */
@Service
public class ShortcutServiceDefaultImpl implements ShortcutService {

    @Override
    public List<ShortcutDto> getShortcuts(long userId) {
        List<ShortcutDto> shortcuts = new ArrayList<>();

        for (String key : ShortcutConfig.DEFAULT_ACTIVE_KEYS) {
            String name = ShortcutConfig.REGISTRY.getOrDefault(key, key);
            // TODO: 后续从各子服务查询 visible / badgeCount
            shortcuts.add(ShortcutDto.builder()
                    .key(key)
                    .name(name)
                    .visible(true)
                    .badgeCount(0L)
                    .linkUrl(ShortcutConfig.getLinkUrl(key))
                    .build());
        }

        return shortcuts;
    }
}
