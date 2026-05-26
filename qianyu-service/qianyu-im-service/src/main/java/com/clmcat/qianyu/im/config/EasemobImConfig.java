package com.clmcat.qianyu.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 环信 IM 配置
 * Client Secret 严格保密，仅服务端使用
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qianyu.im.easemob")
public class EasemobImConfig {

    /** 服务器地址，如 https://a1.easemob.com */
    private String host;

    /** 组织名称 */
    private String orgName;

    /** 应用名称 */
    private String appName;

    /** Client ID（服务端使用，不下发客户端） */
    private String clientId;

    /** Client Secret（严格保密，仅服务端使用） */
    private String clientSecret;

    /** AppKey 组合值: {orgName}#{appName} */
    public String getAppKey() {
        return orgName + "#" + appName;
    }
}
