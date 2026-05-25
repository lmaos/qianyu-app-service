-- 切换到自己需要的数据库
use `qianyu`;


DROP TABLE IF EXISTS `moment`;

CREATE TABLE IF NOT EXISTS `moment` (
    `moment_id` BIGINT NOT NULL COMMENT '作品ID（雪花）',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `moment_type` VARCHAR(16) NOT NULL DEFAULT 'text' COMMENT '作品类型，text, image, video',
    `content` JSON COMMENT '内容, 作品JSON数据, MomentContent',
    `status` TINYINT NOT NULL DEFAULT 0,
    `likes` BIGINT NOT NULL DEFAULT 0,
    `comments` BIGINT NOT NULL DEFAULT 0,
    `shares` BIGINT NOT NULL DEFAULT 0,
    `location` POINT NOT NULL COMMENT '经纬度',
    `country` CHAR(2) DEFAULT NULL COMMENT '国家代码（ISO 3166-1 alpha-2），如 CN, US',
    `create_time` BIGINT NOT NULL COMMENT '客户端时间戳',
    `create_time_server` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `update_time_server` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`moment_id`),
    KEY `idx_author_time` (`author_id`, `create_time_server`),
    KEY `idx_country` (`country`),
    SPATIAL INDEX `idx_location` (`location`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品表';