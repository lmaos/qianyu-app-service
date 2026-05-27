package com.clmcat.qianyu.storage.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件存储记录实体
 *
 * @author author
 * @date 2025-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("storage_file")
public class StorageFile {

    @Id(keyType = KeyType.Auto)
    @Column(value = "id", comment = "自增主键")
    private Long id;

    @Column(value = "user_id", comment = "上传用户ID")
    private Long userId;

    @Column(value = "original_name", comment = "原始文件名")
    private String originalName;

    @Column(value = "file_key", comment = "存储桶内唯一标识")
    private String fileKey;

    @Column(value = "file_url", comment = "文件访问URL")
    private String fileUrl;

    @Column(value = "file_type", comment = "文件扩展名（小写）")
    private String fileType;

    @Column(value = "content_type", comment = "MIME 类型")
    private String contentType;

    @Column(value = "file_size", comment = "文件大小（字节）")
    private Long fileSize;

    @Column(value = "platform", comment = "存储平台标识")
    private String platform;

    @Column(value = "create_time", comment = "上传时间戳（毫秒）")
    private Long createTime;

    @Column(value = "create_time_server", comment = "服务端创建时间")
    private java.time.LocalDateTime createTimeServer;
}
