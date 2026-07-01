package com.clmcat.qianyu.app.service.impl;

import com.clmcat.qianyu.app.mapper.AppConfigMapper;
import com.clmcat.qianyu.app.model.entity.AppConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * App 启动配置服务。
 * <p>
 * 从 app_config 表查询所有公开配置，按 section 分组返回给客户端。
 *
 * @author ark-home
 * @date 2026-06-26
 */
@Service
public class AppConfigService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource
    private AppConfigMapper appConfigMapper;

    /**
     * 获取全部配置，按 section 分组返回。
     * <p>
     * 示例返回：
     * <pre>
     * {
     *   "features": {
     *     "live": {"enabled": true},
     *     "gift": {"enabled": false}
     *   },
     *   "ui": {
     *     "home_tabs": ["recommend", "follow"],
     *     "bottom_nav": [...]
     *   }
     * }
     * </pre>
     */
    public Map<String, Map<String, Object>> getAllConfig() {
        List<AppConfig> list = appConfigMapper.selectAll();

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (AppConfig cfg : list) {
            result.computeIfAbsent(cfg.getSection(), k -> new LinkedHashMap<>())
                    .put(cfg.getConfigKey(), parseValue(cfg));
        }
        return result;
    }

    /**
     * 按 valueType 解析 configValue：json 类型解析为 Map/List，其余原样返回字符串。
     */
    private static Object parseValue(AppConfig cfg) {
        if ("json".equals(cfg.getValueType()) && cfg.getConfigValue() != null) {
            try {
                return OBJECT_MAPPER.readValue(cfg.getConfigValue(), new TypeReference<Object>() {});
            } catch (Exception e) {
                // 解析失败时退回原字符串
            }
        }
        return cfg.getConfigValue();
    }
}
