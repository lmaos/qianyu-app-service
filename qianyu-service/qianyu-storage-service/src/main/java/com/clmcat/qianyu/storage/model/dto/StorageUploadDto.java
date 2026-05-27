package com.clmcat.qianyu.storage.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传参数
 *
 * @author author
 * @date 2025-01-01
 */
@Data
@Schema(description = "文件上传参数")
public class StorageUploadDto {

    @Schema(description = "存储平台（可选，默认使用配置的默认平台）")
    private String platform;

    @Schema(description = "存储路径前缀，默认 upload")
    private String path;
}
