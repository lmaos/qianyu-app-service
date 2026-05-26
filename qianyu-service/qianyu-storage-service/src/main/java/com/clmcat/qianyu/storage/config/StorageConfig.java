package com.clmcat.qianyu.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 存储全局配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qianyu.storage")
public class StorageConfig {

    /** 默认存储平台（用户未指定时使用） */
    private String defaultPlatform = "aliyun";

    /** 上传文件最大大小（字节），默认 10MB */
    private long maxFileSize = 10 * 1024 * 1024;

    /** 允许上传的文件扩展名白名单（小写，不含点号），为空表示不限制 */
    private List<String> allowedExtensions = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp",
            "mp4", "mov", "avi",
            "mp3", "wav", "aac",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "zip", "rar"
    );
}
