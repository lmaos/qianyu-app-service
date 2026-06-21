-- 在这个数据库创建
USE `qianyu`;

-- 文件存储记录表
DROP TABLE IF EXISTS `storage_file`;
CREATE TABLE `storage_file` (
      `id`                  BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '自增主键',
      `user_id`             BIGINT        NOT NULL                 COMMENT '上传用户ID',
      `original_name`       VARCHAR(255)  DEFAULT ''               COMMENT '原始文件名',
      `file_key`            VARCHAR(512)  NOT NULL                 COMMENT '存储桶内唯一标识',
      `file_url`            VARCHAR(1024) NOT NULL                 COMMENT '文件访问URL',
      `file_type`           VARCHAR(32)   DEFAULT ''               COMMENT '文件扩展名（小写）',
      `content_type`        VARCHAR(128)  DEFAULT ''               COMMENT 'MIME 类型',
      `file_size`           BIGINT        DEFAULT 0                COMMENT '文件大小（字节）',
      `platform`            VARCHAR(32)   NOT NULL                 COMMENT '存储平台标识',
      `create_time`         BIGINT        DEFAULT 0                COMMENT '上传时间戳（毫秒）',
      `create_time_server`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '服务端创建时间',
      PRIMARY KEY (`id`),
      KEY `idx_user_id` (`user_id`),
      KEY `idx_file_key` (`file_key`(128)),
      KEY `idx_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储记录';
