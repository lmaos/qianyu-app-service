-- 切换到自己需要的数据库

use `qianyu`;

DROP TABLE IF EXISTS `follow`;
CREATE TABLE IF NOT EXISTS `follow` (
    `id` BIGINT NOT NULL COMMENT '雪花主键',
    `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
    `followee_id` BIGINT NOT NULL COMMENT '被关注者ID',
    `is_friend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为双向好友：0否，1是',
    `client_time` BIGINT NOT NULL COMMENT '客户端时间戳(毫秒)',
    `server_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '服务器UTC时间',
    `update_time_server` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '服务端更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`follower_id`, `followee_id`),
    KEY `idx_follower_time` (`follower_id`, `server_time`),
    KEY `idx_follower_friend` (`follower_id`, `is_friend`, `server_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表（记录谁关注了谁）';

DROP TABLE IF EXISTS `follower`;
CREATE TABLE IF NOT EXISTS `follower` (
    `id` BIGINT NOT NULL COMMENT '雪花主键',
    `followee_id` BIGINT NOT NULL COMMENT '被关注者ID（拥有粉丝的用户）',
    `follower_id` BIGINT NOT NULL COMMENT '粉丝ID',
    `is_friend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为双向好友：0否，1是',
    `client_time` BIGINT NOT NULL COMMENT '客户端时间戳(毫秒)',
    `server_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '服务器UTC时间',
    `update_time_server` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '服务端更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower` (`followee_id`, `follower_id`),
    KEY `idx_followee_time` (`followee_id`, `server_time`),
    KEY `idx_followee_friend` (`followee_id`, `is_friend`, `server_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粉丝表（记录用户的粉丝）';