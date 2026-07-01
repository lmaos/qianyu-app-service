package com.clmcat.qianyu.app.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.qianyu.app.service.impl.AppConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * App 设置与启动配置接口。
 * <p>
 * 提供 App 启动时读取的全局配置，无需登录。
 *
 * @author ark-home
 * @date 2026-06-26
 */
@Tag(name = "App 配置接口", description = "提供 App 启动配置查询，无需登录。")
@ApiController
@RequestMapping("/api/app/setting")
public class SettingController {

    @Resource
    private AppConfigService appConfigService;

    /**
     * App 启动时读取全局配置。
     * <p>
     * 返回按 section 分组的配置，客户端按用途取用。
     * 当前无需登录，后续如果需要用户维度的差异化配置可加 token 参数。
     *
     * @return 全局配置 Map，key 为 section，value 为 section 内的配置键值对
     */
    @Operation(summary = "获取 App 启动配置",
            description = "返回按 section 分组的全局配置，无需登录。客户端可按 config_key 取对应的配置值。")
    @GetMapping("/getconfig")
    public Map<String, Map<String, Object>> getConfig() {
        return appConfigService.getAllConfig();
    }
}
