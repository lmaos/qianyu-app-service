package com.clmcat.qianyu.storage.provider;

import java.io.InputStream;

/**
 * 存储厂商 Provider 接口
 * 每个厂商实现此接口，负责文件上传、删除和访问链接生成
 */
public interface StorageProvider {

    /**
     * 上传文件
     *
     * @param key         文件在存储桶中的唯一标识（路径）
     * @param inputStream 文件输入流
     * @param contentType 文件内容类型
     * @return 文件访问 URL
     */
    String upload(String key, InputStream inputStream, String contentType);

    /**
     * 上传文件（带内容长度）
     *
     * @param key         文件在存储桶中的唯一标识（路径）
     * @param inputStream 文件输入流
     * @param contentType 文件内容类型
     * @param contentLength 文件大小（字节）
     * @return 文件访问 URL
     */
    String upload(String key, InputStream inputStream, String contentType, long contentLength);

    /**
     * 删除文件
     *
     * @param key 文件在存储桶中的唯一标识
     */
    void delete(String key);

    /**
     * 生成预签名访问 URL（临时访问链接）
     *
     * @param key        文件在存储桶中的唯一标识
     * @param expireSeconds URL 有效期（秒）
     * @return 预签名 URL
     */
    String generatePresignedUrl(String key, long expireSeconds);

    /**
     * 返回此 Provider 负责的平台标识
     *
     * @return 平台: aliyun / tencent
     */
    String getPlatform();
}
