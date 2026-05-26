package com.clmcat.qianyu.storage.provider;

import com.clmcat.qianyu.storage.config.TencentCosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 腾讯云 COS Provider
 */
@Slf4j
@Service
public class TencentCosProvider implements StorageProvider {

    @Resource
    private TencentCosConfig config;

    private COSClient cosClient;

    @PostConstruct
    public void init() {
        COSCredentials cred = new BasicCOSCredentials(config.getSecretId(), config.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(config.getRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(cred, clientConfig);
        log.info("腾讯云 COS 客户端初始化完成");
    }

    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
            log.info("腾讯云 COS 客户端已关闭");
        }
    }

    @Override
    public String upload(String key, InputStream inputStream, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        PutObjectRequest request = new PutObjectRequest(config.getBucketName(), key, inputStream, metadata);
        cosClient.putObject(request);
        return buildUrl(key);
    }

    @Override
    public String upload(String key, InputStream inputStream, String contentType, long contentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(contentLength);
        PutObjectRequest request = new PutObjectRequest(config.getBucketName(), key, inputStream, metadata);
        cosClient.putObject(request);
        return buildUrl(key);
    }

    @Override
    public void delete(String key) {
        cosClient.deleteObject(config.getBucketName(), key);
        log.info("腾讯云 COS 删除文件: {}", key);
    }

    @Override
    public String generatePresignedUrl(String key, long expireSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
        URL url = cosClient.generatePresignedUrl(config.getBucketName(), key, expiration);
        return url.toString();
    }

    @Override
    public String getPlatform() {
        return "tencent";
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
            return domain + "/" + key;
        }
        return "https://" + config.getBucketName() + ".cos." + config.getRegion() + ".myqcloud.com/" + key;
    }
}
