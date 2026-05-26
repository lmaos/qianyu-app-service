package com.clmcat.qianyu.im.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.resource.Initializable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 IM 配置
 * 密钥信息仅存服务端，绝不下发到客户端
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "qianyu.im.tencent")
public class TencentImConfig implements InitializingBean {

    /** 应用标识 SDKAppID */
    private long sdkAppId;

    /** 应用密钥 SecretKey（严格保密，仅服务端使用） */
    private String secretKey;

    /** 管理员账号标识，用于 REST API 鉴权 */
    private String adminIdentifier = "administrator";

    /** UserSig 有效期（秒），默认 180 天 */
    private long userSigExpireSeconds = 86400L * 180;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("sdk-appId:{}",sdkAppId);
    }
}
