use `qianyu`;

DROP TABLE IF EXISTS `moment_like`;
CREATE TABLE IF NOT EXISTS `moment_like` (
    `id` BIGINT NOT NULL COMMENT '点赞记录ID（雪花）',
    `moment_id` BIGINT NOT NULL COMMENT '作品ID',
    `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
    `author_id` BIGINT NOT NULL COMMENT '作品作者ID',
    `client_time` BIGINT NOT NULL COMMENT '客户端时间戳(毫秒)',
    `server_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '服务器UTC时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_moment_like` (`moment_id`, `user_id`),
    KEY `idx_moment_like_time` (`moment_id`, `id`),
    KEY `idx_user_moment_like` (`user_id`, `id`),
    KEY `idx_author_moment_like` (`author_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品点赞表';

DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE IF NOT EXISTS `comment_like` (
    `id` BIGINT NOT NULL COMMENT '点赞记录ID（雪花）',
    `comment_id` BIGINT NOT NULL COMMENT '评论ID',
    `moment_id` BIGINT NOT NULL COMMENT '所属作品ID',
    `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
    `author_id` BIGINT NOT NULL COMMENT '评论作者ID',
    `client_time` BIGINT NOT NULL COMMENT '客户端时间戳(毫秒)',
    `server_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '服务器UTC时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_like` (`comment_id`, `user_id`),
    KEY `idx_comment_like_time` (`comment_id`, `id`),
    KEY `idx_user_comment_like` (`user_id`, `id`),
    KEY `idx_author_comment_like` (`author_id`, `id`),
    KEY `idx_moment_comment_like` (`moment_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表（含回复点赞）';
