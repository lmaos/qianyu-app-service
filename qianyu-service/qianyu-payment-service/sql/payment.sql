use qianyu;

-- ============================================================
-- 虚拟币消费侧
-- ============================================================

-- 用户虚拟币钱包（每用户一条记录）
DROP TABLE IF EXISTS user_wallet;
CREATE TABLE user_wallet (
    user_id        BIGINT   NOT NULL  COMMENT '用户ID（主键）',
    balance        BIGINT   NOT NULL DEFAULT 0  COMMENT '可用余额（最小单位，1虚拟币=100最小单位）',
    frozen_balance BIGINT   NOT NULL DEFAULT 0  COMMENT '冻结余额（下单未确认，不可用）',
    total_income   BIGINT   NOT NULL DEFAULT 0  COMMENT '累计收入',
    total_expense  BIGINT   NOT NULL DEFAULT 0  COMMENT '累计支出',
    status         TINYINT  NOT NULL DEFAULT 1  COMMENT '1=正常 2=冻结支出 3=冻结全部',
    version        INT      NOT NULL DEFAULT 0  COMMENT '乐观锁版本号',
    create_time    BIGINT   NOT NULL  COMMENT '创建时间戳（毫秒）',
    update_time    BIGINT   NOT NULL  COMMENT '更新时间戳（毫秒）',

    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户虚拟币钱包';

-- 虚拟币交易流水（每笔余额变动一条记录）
DROP TABLE IF EXISTS transaction_record;
CREATE TABLE transaction_record (
    id                    BIGINT       NOT NULL  COMMENT '流水ID（雪花）',
    trans_no              VARCHAR(64)  NOT NULL  COMMENT '统一交易流水号（关联 gift_send_record / trade_order / settlement_record）',
    user_id               BIGINT       NOT NULL  COMMENT '用户ID',
    trans_type            TINYINT      NOT NULL  COMMENT '1=收入 2=支出',
    amount                BIGINT       NOT NULL  COMMENT '交易金额（最小单位）',
    balance_before        BIGINT       NOT NULL  COMMENT '交易前余额',
    balance_after         BIGINT       NOT NULL  COMMENT '交易后余额',
    biz_type              VARCHAR(32)  NOT NULL  COMMENT '业务类型（gift/live_room/recharge/refund/...）',
    biz_id                VARCHAR(64)  DEFAULT '' COMMENT '业务单号',
    idempotent_key        VARCHAR(64)  DEFAULT '' COMMENT '幂等键（唯一索引，防重）',
    status                TINYINT      NOT NULL DEFAULT 1  COMMENT '1=成功 2=已回退',
    counterparty_user_id  BIGINT       DEFAULT NULL  COMMENT '对手方用户ID（送礼场景=收款主播）',
    correlation_id        BIGINT       DEFAULT NULL  COMMENT '关联流水ID（配对的另一条流水）',
    refund_id             BIGINT       DEFAULT NULL  COMMENT '退款关联原流水ID',
    remark                VARCHAR(256) DEFAULT '' COMMENT '备注',
    create_time           BIGINT       NOT NULL  COMMENT '创建时间戳（毫秒）',

    PRIMARY KEY (id),
    UNIQUE KEY uk_idempotent_key (idempotent_key),
    INDEX idx_trans_no (trans_no),
    INDEX idx_user_time (user_id, create_time),
    INDEX idx_refund_id (refund_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='虚拟币交易流水';

-- ============================================================
-- 限额风控侧
-- ============================================================

-- 用户单日消费统计（每日一条，用于累计限额）
DROP TABLE IF EXISTS user_daily_stats;
CREATE TABLE user_daily_stats (
    user_id       BIGINT   NOT NULL  COMMENT '用户ID',
    stat_date     DATE     NOT NULL  COMMENT '统计日期',
    daily_expense BIGINT   NOT NULL DEFAULT 0  COMMENT '当日累计支出（最小单位）',
    daily_count   INT      NOT NULL DEFAULT 0  COMMENT '当日交易笔数',
    version       INT      NOT NULL DEFAULT 0  COMMENT '乐观锁版本号',
    create_time   BIGINT   NOT NULL  COMMENT '创建时间戳（毫秒）',
    update_time   BIGINT   NOT NULL  COMMENT '更新时间戳（毫秒）',

    PRIMARY KEY (user_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户单日消费统计';

-- ============================================================
-- 交易订单（消费侧与结算侧的桥梁）
-- ============================================================

-- 交易订单（母表：只负责扣款侧，结算明细由 trade_order_item 承载）
-- 状态机：0=PENDING（已冻结余额）→ 1=SUCCESS（已确认扣款+结算）/ 2=CANCELLED（已取消解冻）/ 3=REFUNDED（成功后退款）
DROP TABLE IF EXISTS trade_order;
CREATE TABLE trade_order (
    id              BIGINT       NOT NULL  COMMENT '订单ID（雪花）',
    trans_no        VARCHAR(64)  NOT NULL  COMMENT '统一交易流水号',
    from_user_id    BIGINT       NOT NULL  COMMENT '付款方用户ID',
    coin_amount     BIGINT       NOT NULL  COMMENT '虚拟币消费总额（最小单位）',
    biz_type        VARCHAR(32)  NOT NULL  COMMENT '业务类型（gift/live_room/...）',
    biz_id          VARCHAR(64)  DEFAULT '' COMMENT '业务单号',
    idempotent_key  VARCHAR(64)  DEFAULT '' COMMENT '幂等键',
    status          TINYINT      NOT NULL DEFAULT 0  COMMENT '0=PENDING 1=SUCCESS 2=CANCELLED 3=REFUNDED',
    create_time     BIGINT       NOT NULL  COMMENT '创建时间戳（毫秒）',

    PRIMARY KEY (id),
    UNIQUE KEY uk_trans_no (trans_no),
    UNIQUE KEY uk_idempotent_key (idempotent_key),
    INDEX idx_from_user (from_user_id, create_time),
    INDEX idx_biz (biz_type, biz_id),
    INDEX idx_status_time (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单（扣款侧母表）';

-- 交易订单子表（结算明细：1个母订单对应 1~N 条结算记录）
DROP TABLE IF EXISTS trade_order_item;
CREATE TABLE trade_order_item (
    id              BIGINT       NOT NULL  COMMENT '子项ID（雪花）',
    order_id        BIGINT       NOT NULL  COMMENT '关联 trade_order.id',
    trans_no        VARCHAR(64)  NOT NULL  COMMENT '消费流水号（冗余，避免 JOIN 查对账）',
    biz_no          VARCHAR(64)  NOT NULL  COMMENT '业务流水号（唯一，单次=trans_no，批量=独立子编号）',
    from_user_id    BIGINT       NOT NULL  COMMENT '付款方用户ID（冗余，避免 JOIN 查"我的送礼"）',
    to_user_id      BIGINT       NOT NULL  COMMENT '收款方用户ID（主播）',
    settle_amount   BIGINT       NOT NULL  COMMENT '结算货币金额（最小单位）',
    commission_rate INT          NOT NULL DEFAULT 0  COMMENT '分佣比例（万分比）',
    status          TINYINT      NOT NULL DEFAULT 0  COMMENT '0=PENDING 1=SUCCESS 2=CANCELLED',
    create_time     BIGINT       NOT NULL  COMMENT '创建时间戳（毫秒）',

    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_no (biz_no),
    INDEX idx_order_id (order_id),
    INDEX idx_trans_no (trans_no),
    INDEX idx_from_user (from_user_id, create_time),
    INDEX idx_to_user (to_user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单子表（结算明细）';

-- ============================================================
-- 结算货币侧（主播收益）
-- ============================================================

-- 主播结算账户（每主播一条记录）
DROP TABLE IF EXISTS host_settlement;
CREATE TABLE host_settlement (
    user_id        BIGINT   NOT NULL  COMMENT '主播用户ID（主键）',
    balance        BIGINT   NOT NULL DEFAULT 0  COMMENT '可结算余额（结算货币最小单位）',
    total_earning  BIGINT   NOT NULL DEFAULT 0  COMMENT '累计收益',
    frozen_balance BIGINT   NOT NULL DEFAULT 0  COMMENT '冻结中（提现审核中）',
    status         TINYINT  NOT NULL DEFAULT 1  COMMENT '1=正常 2=冻结提现',
    version        INT      NOT NULL DEFAULT 0  COMMENT '乐观锁版本号',
    create_time    BIGINT   NOT NULL  COMMENT '创建时间戳（毫秒）',
    update_time    BIGINT   NOT NULL  COMMENT '更新时间戳（毫秒）',

    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主播结算账户';

-- 结算流水（每笔结算变动一条记录。trans_no=消费流水，biz_no=业务流水）
DROP TABLE IF EXISTS settlement_record;
CREATE TABLE settlement_record (
    id              BIGINT       NOT NULL  COMMENT '流水ID（雪花）',
    trans_no        VARCHAR(64)  NOT NULL  COMMENT '消费流水号（来自扣款侧，一次扣款多条结算相同）',
    biz_no          VARCHAR(64)  NOT NULL  COMMENT '业务流水号（唯一，单次结算=trans_no，批量结算=独立子编号）',
    user_id         BIGINT       NOT NULL  COMMENT '主播用户ID',
    settle_type     TINYINT      NOT NULL  COMMENT '1=礼物收益 2=直播收益 3=活动奖励 4=退款',
    amount          BIGINT       NOT NULL  COMMENT '结算金额（最小单位）',
    balance_before  BIGINT       NOT NULL  COMMENT '结算前余额',
    balance_after   BIGINT       NOT NULL  COMMENT '结算后余额',
    commission_rate INT          NOT NULL DEFAULT 0  COMMENT '当时分佣比例（万分比）',
    idempotent_key  VARCHAR(64)  DEFAULT '' COMMENT '幂等键',
    status          TINYINT      NOT NULL DEFAULT 1  COMMENT '1=成功 2=已回退',
    remark          VARCHAR(256) DEFAULT '' COMMENT '备注',
    create_time     BIGINT       NOT NULL  COMMENT '创建时间戳（毫秒）',

    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_no (biz_no),
    UNIQUE KEY uk_idempotent_key (idempotent_key),
    INDEX idx_trans_no (trans_no),
    INDEX idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算流水';
