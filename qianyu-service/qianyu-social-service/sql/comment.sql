use `qianyu`;

DROP TABLE IF EXISTS `moment_comment`;
CREATE TABLE IF NOT EXISTS `moment_comment` (
    `comment_id` BIGINT NOT NULL COMMENT '评论ID（雪花）',
    `moment_id` BIGINT NOT NULL COMMENT '作品ID',
    `moment_author_id` BIGINT NOT NULL COMMENT '作品作者ID',
    `author_id` BIGINT NOT NULL COMMENT '评论作者ID',
    `parent_comment_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父评论ID；一级评论=0，二级回复=一级评论ID',
    `reply_comment_id` BIGINT NOT NULL DEFAULT 0 COMMENT '回复的评论ID',
    `reply_user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '被回复用户ID',
    `comment_level` TINYINT NOT NULL COMMENT '评论层级：1一级评论，2二级回复',
    `content` JSON COMMENT '评论内容 JSON(CommentContent)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0显示，1隐藏，2删除',
    `likes` BIGINT NOT NULL DEFAULT 0 COMMENT '点赞数冗余',
    `replies` BIGINT NOT NULL DEFAULT 0 COMMENT '回复数冗余',
    `client_time` BIGINT NOT NULL COMMENT '客户端时间戳(毫秒)',
    `server_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '服务器UTC时间',
    `update_time_server` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '服务端更新时间',
    PRIMARY KEY (`comment_id`),
    KEY `idx_moment_top_comment` (`moment_id`, `parent_comment_id`, `comment_id`),
    KEY `idx_parent_comment` (`parent_comment_id`, `comment_id`),
    KEY `idx_reply_comment` (`reply_comment_id`, `comment_id`),
    KEY `idx_author_comment` (`author_id`, `comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品评论表（含二级回复）';
