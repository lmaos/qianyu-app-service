package com.clmcat.qianyu.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * IM 全局配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qianyu.im")
public class ImConfig {

    /** 默认 IM 渠道（用户未指定时使用） */
    private String defaultChannel = "tencent";
}
