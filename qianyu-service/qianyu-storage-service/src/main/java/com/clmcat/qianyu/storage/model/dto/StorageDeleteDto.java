package com.clmcat.qianyu.storage.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件删除参数
 */
@Data
@Schema(description = "文件删除参数")
public class StorageDeleteDto {

    @Schema(description = "文件在存储桶中的唯一标识")
    private String key;

    @Schema(description = "存储平台（可选，默认使用配置的默认平台）")
    private String platform;
}
