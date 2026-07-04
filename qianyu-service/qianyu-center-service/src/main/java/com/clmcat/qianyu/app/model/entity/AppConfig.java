package com.clmcat.qianyu.app.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * App 启动配置项表
 *
 * @author ark-home
 * @date 2026-06-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("app_config")
public class AppConfig {

    /**
     * 物理主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 配置段，如 features / ui / limits / upload / app_update / maintenance
     */
    private String section;

    /**
     * 配置键，段内唯一
     */
    private String configKey;

    /**
     * 配置值（JSON 字符串）
     */
    private String configValue;

    /**
     * 值类型：string / number / boolean / json
     */
    private String valueType;

    /**
     * 说明
     */
    private String description;

    /**
     * 更新时间戳（Unix 秒级）
     */
    private Long updateTime;
}
