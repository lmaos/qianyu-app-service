package com.clmcat.qianyu.storage.service;

import com.clmcat.qianyu.storage.config.StorageConfig;
import com.clmcat.qianyu.storage.model.UploadResult;
import com.clmcat.qianyu.storage.provider.StorageProvider;
import com.clmcat.qianyu.storage.router.StorageRouter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 存储业务服务
 * 对外提供统一的上传、删除、预签名链接生成能力
 */
@Slf4j
@Service
public class StorageServiceBiz {

    @Resource
    private StorageRouter storageRouter;

    @Resource
    private StorageConfig storageConfig;

    /**
     * 上传文件（使用默认平台）
     *
     * @param key         文件唯一标识
     * @param inputStream 文件输入流
     * @param contentType 文件内容类型
     * @return 上传结果
     */
    public UploadResult upload(String key, InputStream inputStream, String contentType) {
        return upload(key, inputStream, contentType, -1);
    }

    /**
     * 上传文件（使用默认平台，带内容长度）
     *
     * @param key           文件唯一标识
     * @param inputStream   文件输入流
     * @param contentType   文件内容类型
     * @param contentLength 文件大小
     * @return 上传结果
     */
    public UploadResult upload(String key, InputStream inputStream, String contentType, long contentLength) {
        return upload(storageConfig.getDefaultPlatform(), key, inputStream, contentType, contentLength);
    }

    /**
     * 上传文件（指定平台）
     *
     * @param platform    存储平台
     * @param key         文件唯一标识
     * @param inputStream 文件输入流
     * @param contentType 文件内容类型
     * @return 上传结果
     */
    public UploadResult upload(String platform, String key, InputStream inputStream, String contentType) {
        return upload(platform, key, inputStream, contentType, -1);
    }

    /**
     * 上传文件（指定平台，带内容长度）
     *
     * @param platform      存储平台
     * @param key           文件唯一标识
     * @param inputStream   文件输入流
     * @param contentType   文件内容类型
     * @param contentLength 文件大小
     * @return 上传结果
     */
    public UploadResult upload(String platform, String key, InputStream inputStream, String contentType, long contentLength) {
        StorageProvider provider = storageRouter.route(platform);
        String url;
        if (contentLength > 0) {
            url = provider.upload(key, inputStream, contentType, contentLength);
        } else {
            url = provider.upload(key, inputStream, contentType);
        }
        log.info("文件上传成功，platform: {}, key: {}, url: {}", platform, key, url);
        return new UploadResult(url, key, platform);
    }

    /**
     * 删除文件（使用默认平台）
     *
     * @param key 文件唯一标识
     */
    public void delete(String key) {
        delete(storageConfig.getDefaultPlatform(), key);
    }

    /**
     * 删除文件（指定平台）
     *
     * @param platform 存储平台
     * @param key      文件唯一标识
     */
    public void delete(String platform, String key) {
        StorageProvider provider = storageRouter.route(platform);
        provider.delete(key);
        log.info("文件删除成功，platform: {}, key: {}", platform, key);
    }

    /**
     * 生成预签名 URL（使用默认平台）
     *
     * @param key           文件唯一标识
     * @param expireSeconds 有效期（秒）
     * @return 预签名 URL
     */
    public String generatePresignedUrl(String key, long expireSeconds) {
        return generatePresignedUrl(storageConfig.getDefaultPlatform(), key, expireSeconds);
    }

    /**
     * 生成预签名 URL（指定平台）
     *
     * @param platform      存储平台
     * @param key           文件唯一标识
     * @param expireSeconds 有效期（秒）
     * @return 预签名 URL
     */
    public String generatePresignedUrl(String platform, String key, long expireSeconds) {
        StorageProvider provider = storageRouter.route(platform);
        return provider.generatePresignedUrl(key, expireSeconds);
    }
}
