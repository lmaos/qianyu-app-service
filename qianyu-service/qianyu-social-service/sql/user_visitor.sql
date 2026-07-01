-- 切换到自己需要的数据库
USE `qianyu`;

-- 谁看过我（访客列表）
DROP TABLE IF EXISTS `user_visitor`;
CREATE TABLE IF NOT EXISTS `user_visitor` (
    `id` BIGINT NOT NULL COMMENT '雪花主键',
    `visitor_id` BIGINT NOT NULL COMMENT '访问者用户ID（谁来看的）',
    `visitee_id` BIGINT NOT NULL COMMENT '被访问者用户ID（主页主人）',
    `visit_count` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '累计访问次数',
    `is_new` TINYINT NOT NULL DEFAULT 1 COMMENT '是否新访客：0已读，1未读',
    `client_time` BIGINT NOT NULL COMMENT '最近访问客户端时间戳(毫秒)',
    `server_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '最近访问服务器时间',
    `create_time_server` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '首次访问服务器时间',
    `update_time_server` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '服务端更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_visitee_visitor` (`visitee_id`, `visitor_id`),
    KEY `idx_visitee_new` (`visitee_id`, `is_new`, `server_time`),
    KEY `idx_visitee_time` (`visitee_id`, `server_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客记录表（谁看过我）';

-- 我看过谁（浏览历史）
DROP TABLE IF EXISTS `user_history`;
CREATE TABLE IF NOT EXISTS `user_history` (
    `id` BIGINT NOT NULL COMMENT '雪花主键',
    `visitor_id` BIGINT NOT NULL COMMENT '访问者用户ID（谁去看的）',
    `visitee_id` BIGINT NOT NULL COMMENT '被访问者用户ID（被看的主页主人）',
    `visit_count` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '累计访问次数',
    `client_time` BIGINT NOT NULL COMMENT '最近访问客户端时间戳(毫秒)',
    `server_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '最近访问服务器时间',
    `create_time_server` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '首次访问服务器时间',
    `update_time_server` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '服务端更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_visitor_visitee` (`visitor_id`, `visitee_id`),
    KEY `idx_visitor_time` (`visitor_id`, `server_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表（我看过谁）';
