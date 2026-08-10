use qianyu;

-- ============================================================
-- 礼物系统 & 道具背包
-- ============================================================

-- 礼物配置表（静态定义，所有礼物类型共用）
DROP TABLE IF EXISTS gift_config;
CREATE TABLE gift_config (
    id              BIGINT       NOT NULL  COMMENT '礼物ID（雪花）',
    name            VARCHAR(64)  NOT NULL  COMMENT '礼物名称',
    icon            VARCHAR(512) NOT NULL DEFAULT '' COMMENT '图标URL',
    animation_url   VARCHAR(512) NOT NULL DEFAULT '' COMMENT '动画URL（mp4/webp）',
    price           BIGINT       NOT NULL DEFAULT 0  COMMENT '基础价格（虚拟币最小单位），0=免费/活动获取',

    -- 类型体系
    gift_type       TINYINT      NOT NULL  COMMENT '1=普通 2=盲盒 3=奖池 4=专属 5=活动 6=锁定 7=条件 8=任务',
    category        VARCHAR(32)  NOT NULL DEFAULT 'normal' COMMENT '上架分类：luxury=豪华, normal=普通, special=特殊, free=免费',

    -- 扩展配置（类型差异化，JSON）
    extra_config    TEXT         DEFAULT NULL  COMMENT '扩展配置JSON。盲盒:{"pool_id":1}; 专属:{"host_id":123}; 锁定:{"unlock_vip_level":3}; 条件:{"condition_expr":"user.level>=10"}',

    -- 上架控制
    shelf_scenes    VARCHAR(256) NOT NULL DEFAULT 'live_room' COMMENT '上架场景，逗号分隔：live_room,voice_room,private_chat',
    sort_order      INT          NOT NULL DEFAULT 0  COMMENT '排序权重，越大越靠前',
    status          TINYINT      NOT NULL DEFAULT 1  COMMENT '0=下架 1=上架',

    -- 分佣
    commission_rate INT          NOT NULL DEFAULT 5000 COMMENT '默认分佣比例（万分比），5000=50%',

    -- 动画资源
    animation_duration INT       DEFAULT 0  COMMENT '动画时长（毫秒），0=默认',
    svga_url         VARCHAR(512) DEFAULT '' COMMENT 'SVGA动画URL（备选格式）',

    create_time     BIGINT       NOT NULL  COMMENT '创建时间戳（毫秒）',
    update_time     BIGINT       NOT NULL  COMMENT '更新时间戳（毫秒）',

    PRIMARY KEY (id),
    INDEX idx_gift_type (gift_type),
    INDEX idx_category_sort (category, sort_order),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='礼物配置表';

-- 盲盒掉落配置（权重随机）
DROP TABLE IF EXISTS gift_blindbox_drop;
CREATE TABLE gift_blindbox_drop (
    id              BIGINT   NOT NULL  COMMENT 'ID（雪花）',
    blindbox_gift_id BIGINT  NOT NULL  COMMENT '盲盒礼物ID',
    drop_gift_id    BIGINT   NOT NULL  COMMENT '可开出礼物ID',
    weight          INT      NOT NULL DEFAULT 1  COMMENT '权重（越大概率越高）',
    status          TINYINT  NOT NULL DEFAULT 1  COMMENT '1=启用 2=停用',
    create_time     BIGINT   NOT NULL  COMMENT '创建时间戳（毫秒）',

    PRIMARY KEY (id),
    INDEX idx_blindbox (blindbox_gift_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盲盒掉落配置';

-- 送礼记录（每笔送礼对应一条，财务对账核心表。trans_no=消费流水，biz_no=业务流水）
DROP TABLE IF EXISTS gift_send_record;
CREATE TABLE gift_send_record (
    id                  BIGINT       NOT NULL  COMMENT '送礼记录ID（雪花）',
    trans_no            VARCHAR(64)  NOT NULL  COMMENT '消费流水号（来自扣款侧，一次扣款多条记录相同）',
    biz_no              VARCHAR(64)  NOT NULL  COMMENT '业务流水号（唯一，单次送=trans_no，批量送=独立子编号）',
    sender_user_id      BIGINT       NOT NULL  COMMENT '送礼人用户ID',
    receiver_user_id    BIGINT       NOT NULL  COMMENT '收礼人用户ID（主播）',

    -- 礼物信息（冗余快照，防止礼物配置变更后历史数据失真）
    gift_id             BIGINT       NOT NULL  COMMENT '礼物ID',
    gift_name           VARCHAR(64)  NOT NULL  COMMENT '礼物名称（快照）',
    gift_price          BIGINT       NOT NULL  COMMENT '礼物单价（快照）',
    quantity            INT          NOT NULL DEFAULT 1  COMMENT '赠送数量',
    total_amount        BIGINT       NOT NULL  COMMENT '总金额 = price * quantity',

    -- 实际结算礼物（盲盒场景：gift_id=盲盒，actual_gift_id=开出的礼物）
    actual_gift_id      BIGINT       DEFAULT NULL  COMMENT '实际结算礼物ID（盲盒/概率场景）',
    actual_gift_name    VARCHAR(64)  DEFAULT NULL  COMMENT '实际结算礼物名称',

    -- 场景信息
    scene_type          VARCHAR(32)  NOT NULL DEFAULT 'live_room' COMMENT '场景：live_room/voice_room/private_chat',
    room_id             BIGINT       DEFAULT NULL  COMMENT '直播间ID',

    -- 支付方式
    pay_type            TINYINT      NOT NULL DEFAULT 1  COMMENT '支付方式：1=虚拟币 2=背包礼物',

    -- 幂等
    idempotent_key      VARCHAR(64)  DEFAULT '' COMMENT '幂等键',

    -- 分成信息（快照，用于对账）
    commission_rate     INT          NOT NULL DEFAULT 0  COMMENT '分佣比例快照（万分比）',
    settle_amount       BIGINT       NOT NULL DEFAULT 0  COMMENT '主播实际结算金额',

    status              TINYINT      NOT NULL DEFAULT 1  COMMENT '1=SUCCESS 2=REFUNDED',
    remark              VARCHAR(256) DEFAULT '' COMMENT '备注',
    create_time         BIGINT       NOT NULL  COMMENT '送礼时间戳（毫秒）',

    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_no (biz_no),
    UNIQUE KEY uk_idempotent_key (idempotent_key),
    INDEX idx_trans_no (trans_no),
    INDEX idx_sender_time (sender_user_id, create_time),
    INDEX idx_receiver_time (receiver_user_id, create_time),
    INDEX idx_room_time (room_id, create_time),
    INDEX idx_gift_id (gift_id),
    INDEX idx_scene_room (scene_type, room_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='送礼记录表';

-- 用户礼物库存（背包，聚合存储：同一用户+同一礼物+同一过期日期=一行）
DROP TABLE IF EXISTS user_gift_inventory;
CREATE TABLE user_gift_inventory (
    id              BIGINT       NOT NULL  COMMENT '库存记录ID（雪花）',
    user_id         BIGINT       NOT NULL  COMMENT '用户ID',
    gift_id         BIGINT       NOT NULL  COMMENT '礼物ID',
    quantity        INT          NOT NULL DEFAULT 0  COMMENT '持有数量',

    -- 来源追溯
    source_type     VARCHAR(32)  NOT NULL  COMMENT '来源：activity/task/event/admin_grant/refund',
    source_id       VARCHAR(64)  DEFAULT '' COMMENT '来源单号（活动ID/任务ID等）',

    -- 过期（自然日整点过期）
    expire_time     BIGINT       NOT NULL  COMMENT '过期时间戳（毫秒），取当日23:59:59，0=永久',

    status          TINYINT      NOT NULL DEFAULT 1  COMMENT '1=有效 2=已过期 3=已用完',
    create_time     BIGINT       NOT NULL  COMMENT '创建时间戳（毫秒）',
    update_time     BIGINT       NOT NULL  COMMENT '更新时间戳（毫秒）',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_gift_expire (user_id, gift_id, expire_time),
    INDEX idx_user_id (user_id),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户礼物库存表';

-- 道具配置表
DROP TABLE IF EXISTS prop_config;
CREATE TABLE prop_config (
    id              BIGINT       NOT NULL  COMMENT '道具ID（雪花）',
    name            VARCHAR(64)  NOT NULL  COMMENT '道具名称',
    icon            VARCHAR(512) NOT NULL DEFAULT '' COMMENT '图标URL',
    prop_type       VARCHAR(32)  NOT NULL  COMMENT '道具类型：mount/frame/bubble/tag/title/other',

    -- 道具行为
    usage_type      TINYINT      NOT NULL DEFAULT 1  COMMENT '1=穿戴（可穿脱） 2=消耗（使用即销毁）',

    -- 效果描述
    animation_url   VARCHAR(512) DEFAULT '' COMMENT '特效URL（座驾进场动画等）',
    extra_config    TEXT         DEFAULT NULL  COMMENT '扩展配置JSON',

    valid_days      INT          NOT NULL DEFAULT 30  COMMENT '有效天数（从获取日起算），0=永久',

    status          TINYINT      NOT NULL DEFAULT 1  COMMENT '0=下架 1=上架',
    create_time     BIGINT       NOT NULL  COMMENT '创建时间戳（毫秒）',
    update_time     BIGINT       NOT NULL  COMMENT '更新时间戳（毫秒）',

    PRIMARY KEY (id),
    INDEX idx_prop_type (prop_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='道具配置表';

-- 用户道具库存（道具一个是一个，不聚合）
DROP TABLE IF EXISTS user_prop_inventory;
CREATE TABLE user_prop_inventory (
    id              BIGINT       NOT NULL  COMMENT '道具记录ID（雪花）',
    user_id         BIGINT       NOT NULL  COMMENT '用户ID',
    prop_id         BIGINT       NOT NULL  COMMENT '道具配置ID',
    prop_type       VARCHAR(32)  NOT NULL  COMMENT '道具类型（冗余，方便查询）',

    -- 穿戴/使用状态
    status          TINYINT      NOT NULL DEFAULT 0  COMMENT '0=背包中 1=穿戴中 2=已使用（消耗型） 3=已过期',

    -- 来源追溯
    source_type     VARCHAR(32)  NOT NULL  COMMENT '来源：activity/vip_upgrade/admin_grant/...',
    source_id       VARCHAR(64)  DEFAULT '' COMMENT '来源单号',

    -- 过期
    obtain_time     BIGINT       NOT NULL  COMMENT '获取时间戳（毫秒）',
    expire_time     BIGINT       NOT NULL  COMMENT '过期时间戳（毫秒），0=永久',

    create_time     BIGINT       NOT NULL  COMMENT '创建时间戳（毫秒）',
    update_time     BIGINT       NOT NULL  COMMENT '更新时间戳（毫秒）',

    PRIMARY KEY (id),
    INDEX idx_user_type_status (user_id, prop_type, status),
    INDEX idx_user_expire (user_id, expire_time),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户道具库存表';
