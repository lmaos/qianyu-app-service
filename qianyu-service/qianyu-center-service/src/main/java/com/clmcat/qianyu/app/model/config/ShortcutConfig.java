package com.clmcat.qianyu.app.model.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 快捷入口静态配置注册表。
 * <p>
 * 前端支持的完整 key 列表，后续各入口的可见性/角标等动态数据由 ShortcutService 提供。
 */
public final class ShortcutConfig {

    private ShortcutConfig() {
    }

    /** 全部快捷入口 key → name 映射 */
    public static final Map<String, String> REGISTRY = Map.ofEntries(
            Map.entry("wallet", "我的钱包"),
            Map.entry("orders", "订单中心"),
            Map.entry("merchant", "商家管理"),
            Map.entry("anchor", "主播中心"),
            Map.entry("friends", "添加朋友"),
            Map.entry("visitors", "新访客"),
            Map.entry("settings", "更多设置"),
            Map.entry("edit", "编辑主页"),
            Map.entry("all-functions", "全部功能")
    );

    /** 页面跳转协议 linkUrl */
    public static final Map<String, String> LINK_URLS = Map.ofEntries(
            Map.entry("wallet", "page://open?page=/pages/user/wallet"),
            Map.entry("orders", "page://open?page=/pages/user/order-list"),
            Map.entry("merchant", "page://open?page=/pages/user/merchant-center"),
            Map.entry("anchor", "page://open?page=/pages/user/anchor-center"),
            Map.entry("friends", "page://open?page=/pages/user/add-friend"),
            Map.entry("visitors", "page://open?page=/pages/user/visitor-list"),
            Map.entry("settings", "page://open?page=/pages/user/more-settings"),
            Map.entry("edit", "page://open?page=/pages/user/edit-profile"),
            Map.entry("all-functions", "page://open?page=/pages/user/all-functions")
    );

    /** 本期默认展示的入口 key */
    public static final List<String> DEFAULT_ACTIVE_KEYS = List.of(
            "anchor",    // 主播中心
            "merchant",  // 商家管理
            "orders",    // 订单中心
            "wallet"     // 我的钱包
    );

    /** 全部入口 key 集合 */
    public static final Set<String> ALL_KEYS = REGISTRY.keySet();

    /**
     * 根据 key 获取 linkUrl，未配置时返回空字符串。
     */
    public static String getLinkUrl(String key) {
        return LINK_URLS.getOrDefault(key, "");
    }
}
