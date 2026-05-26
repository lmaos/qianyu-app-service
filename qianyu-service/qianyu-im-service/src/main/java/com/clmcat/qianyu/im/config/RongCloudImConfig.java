package com.clmcat.qianyu.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 融云 IM 配置
 * App Secret 严格保密，仅服务端使用
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qianyu.im.rongcloud")
public class RongCloudImConfig {

    /** API 地址，如 https://api-cn.rongcloud.com */
    private String apiUrl;

    /** 应用 App Key（公开，可下发客户端） */
    private String appKey;

    /** 应用 App Secret（严格保密，仅服务端使用） */
    private String appSecret;
}
