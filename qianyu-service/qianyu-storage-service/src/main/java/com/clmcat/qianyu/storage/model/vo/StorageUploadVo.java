package com.clmcat.qianyu.storage.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果
 *
 * @author author
 * @date 2025-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文件上传结果")
public class StorageUploadVo {

    @Schema(description = "文件记录ID")
    private Long id;

    @Schema(description = "文件访问 URL")
    private String url;

    @Schema(description = "文件在存储桶中的唯一标识")
    private String key;

    @Schema(description = "存储平台标识")
    private String platform;

    @Schema(description = "文件扩展名")
    private String fileType;
}
