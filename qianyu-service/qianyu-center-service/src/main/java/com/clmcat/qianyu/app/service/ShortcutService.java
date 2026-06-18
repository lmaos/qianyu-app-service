package com.clmcat.qianyu.app.service;

import com.clmcat.qianyu.app.api.model.dto.ShortcutDto;

import java.util.List;

/**
 * 快捷入口服务。
 * <p>
 * 根据 userId 返回该用户可见的快捷入口列表。
 * 后续接入各子服务（直播、商城、支付等）查询可见性和角标时，替换实现即可。
 */
public interface ShortcutService {

    /**
     * 获取用户可见的快捷入口列表。
     *
     * @param userId 用户ID
     * @return 快捷入口列表
     */
    List<ShortcutDto> getShortcuts(long userId);
}
