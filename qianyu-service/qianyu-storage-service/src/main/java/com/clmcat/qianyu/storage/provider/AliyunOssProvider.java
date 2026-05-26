package com.clmcat.qianyu.storage.provider;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.clmcat.qianyu.storage.config.AliyunOssConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 阿里云 OSS Provider
 */
@Slf4j
@Service
public class AliyunOssProvider implements StorageProvider {

    @Resource
    private AliyunOssConfig config;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(
                config.getEndpoint(),
                config.getAccessKeyId(),
                config.getAccessKeySecret()
        );
        log.info("阿里云 OSS 客户端初始化完成");
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("阿里云 OSS 客户端已关闭");
        }
    }

    @Override
    public String upload(String key, InputStream inputStream, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        ossClient.putObject(config.getBucketName(), key, inputStream, metadata);
        return buildUrl(key);
    }

    @Override
    public String upload(String key, InputStream inputStream, String contentType, long contentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(contentLength);
        ossClient.putObject(config.getBucketName(), key, inputStream, metadata);
        return buildUrl(key);
    }

    @Override
    public void delete(String key) {
        ossClient.deleteObject(config.getBucketName(), key);
        log.info("阿里云 OSS 删除文件: {}", key);
    }

    @Override
    public String generatePresignedUrl(String key, long expireSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
        URL url = ossClient.generatePresignedUrl(config.getBucketName(), key, expiration);
        return url.toString();
    }

    @Override
    public String getPlatform() {
        return "aliyun";
    }

    /**
     * 构建文件访问 URL
     */
    private String buildUrl(String key) {
        if (config.getCustomDomain() != null && !config.getCustomDomain().isBlank()) {
            String domain = config.getCustomDomain();
            if (!domain.startsWith("http")) {
                domain = "https://" + domain;
            }
            // 避免 customDomain 末尾带 / 导致双斜杠
            if (domain.endsWith("/")) {
                domain = domain.substring(0, domain.length() - 1);
            }
            return domain + "/" + key;
        }
        return "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + key;
    }
}
