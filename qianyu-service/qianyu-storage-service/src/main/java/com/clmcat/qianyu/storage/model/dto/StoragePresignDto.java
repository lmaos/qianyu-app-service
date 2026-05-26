package com.clmcat.qianyu.storage.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 预签名链接生成参数
 */
@Data
@Schema(description = "预签名链接生成参数")
public class StoragePresignDto {

    @Schema(description = "文件在存储桶中的唯一标识")
    private String key;

    @Schema(description = "链接有效期（秒），默认 3600")
    private Long expireSeconds = 3600L;

    @Schema(description = "存储平台（可选，默认使用配置的默认平台）")
    private String platform;
}
