package com.clmcat.qianyu.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {

    /** 文件访问 URL */
    private String url;

    /** 文件在存储桶中的唯一标识 */
    private String key;

    /** 存储平台 */
    private String platform;
}
