use qianyu;
-- 直播间主表（低频写）
CREATE TABLE live_room (
                           id              BIGINT       NOT NULL  COMMENT '内部逻辑主键（雪花ID），永远不变',
                           room_no         BIGINT       NOT NULL  COMMENT '对外直播间编号，默认==id，UNIQUE，用于搜索/分享/链接',
                           anchor_user_id  BIGINT       NOT NULL  COMMENT '主播用户ID',
                           title           VARCHAR(128) NOT NULL  COMMENT '直播间标题',
                           cover_image     VARCHAR(512) DEFAULT '' COMMENT '封面图URL',
                           status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0=待开播 1=直播中 2=已结束',
                           start_time      BIGINT       NOT NULL DEFAULT 0 COMMENT '开播时间戳（毫秒）',
                           end_time        BIGINT       NOT NULL DEFAULT 0 COMMENT '关播时间戳（毫秒）',
                           create_time     BIGINT       NOT NULL  COMMENT '创建时间戳（毫秒）',
                           update_time     BIGINT       NOT NULL  COMMENT '更新时间戳（毫秒）',

                           PRIMARY KEY (id),
                           UNIQUE KEY uk_room_no (room_no),
                           INDEX idx_anchor_user_id (anchor_user_id),
                           INDEX idx_status_room_no (status, room_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播间主表';

-- 计数器表（高频写，本场计数，开播清零）
CREATE TABLE live_room_count (
                                 room_id           BIGINT  NOT NULL  COMMENT '关联 live_room.id',
                                 viewer_count      BIGINT  NOT NULL DEFAULT 0 COMMENT '本场观看人数',
                                 max_online_count  BIGINT  NOT NULL DEFAULT 0 COMMENT '本场峰值在线人数',
                                 like_count        BIGINT  NOT NULL DEFAULT 0 COMMENT '本场点赞数',
                                 gift_count        BIGINT  NOT NULL DEFAULT 0 COMMENT '本场礼物数',
                                 gift_amount       BIGINT  NOT NULL DEFAULT 0 COMMENT '本场礼物金额（分）',
                                 comment_count     BIGINT  NOT NULL DEFAULT 0 COMMENT '本场评论/弹幕数',
                                 share_count       BIGINT  NOT NULL DEFAULT 0 COMMENT '本场分享次数',

                                 PRIMARY KEY (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播间计数器表';