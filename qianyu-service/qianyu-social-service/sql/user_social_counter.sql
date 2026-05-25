-- 可以改自己的库
USE `qianyu`;
DROP TABLE IF EXISTS `user_social_counter`;
CREATE TABLE IF NOT EXISTS `user_social_counter` (
 `user_id` BIGINT NOT NULL COMMENT '用户ID',

-- 作品统计
 `post_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '作品总数',
 `image_post_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '图片作品数',
 `video_post_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '视频作品数',
 `text_post_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文本作品数',

-- 收到的互动反馈
 `like_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收到的总点赞数',
 `comment_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收到的总评论数',
 `share_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '作品被分享总数',
 `favorite_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '作品被收藏总数',

-- 社交关系
 `follow_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '关注数',
 `follower_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '粉丝数',
 `friend_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '好友数（互关）',

-- 主动互动行为
 `liked_post_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '我点赞过的作品数',
 `favorited_post_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '我收藏过的作品数',
 `commented_post_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '我评论过的作品数',
 `version` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
 PRIMARY KEY (`user_id`)
    ) ENGINE=InnoDB COMMENT='用户社交统计表';