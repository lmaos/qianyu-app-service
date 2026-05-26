package com.clmcat.qianyu.storage.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "qianyu.storage.aliyun")
public class AliyunOssConfig implements InitializingBean {

    /** 访问密钥 ID */
    private String accessKeyId;

    /** 访问密钥 Secret */
    private String accessKeySecret;

    /** Endpoint 地域节点，如 oss-cn-hangzhou.aliyuncs.com */
    private String endpoint;

    /** 存储桶名称 */
    private String bucketName;

    /** 自定义域名（可选，配置后 URL 使用自定义域名） */
    private String customDomain;

    @Override
    public void afterPropertiesSet() {
        log.info("阿里云 OSS 配置加载完成，bucket: {}", bucketName);
    }
}
