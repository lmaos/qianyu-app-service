-- 用户钱包
CREATE TABLE user_wallet (
                             user_id        BIGINT NOT NULL,
                             balance        BIGINT NOT NULL DEFAULT 0  COMMENT '可用余额（最小单位）',
                             total_income   BIGINT NOT NULL DEFAULT 0  COMMENT '累计收入',
                             total_expense  BIGINT NOT NULL DEFAULT 0  COMMENT '累计支出',
                             version        INT NOT NULL DEFAULT 0     COMMENT '乐观锁版本号',
                             create_time    BIGINT NOT NULL,
                             update_time    BIGINT NOT NULL,
                             PRIMARY KEY (user_id)
);

-- 交易流水
CREATE TABLE transaction_record (
                                    id              BIGINT NOT NULL             COMMENT '雪花ID',
                                    trans_no        BIGINT NOT NULL             COMMENT '流水号（对外展示，默认=id）',
                                    user_id         BIGINT NOT NULL             COMMENT '用户ID',
                                    trans_type      TINYINT NOT NULL            COMMENT '1=收入 2=支出',
                                    amount          BIGINT NOT NULL             COMMENT '交易金额',
                                    balance_before  BIGINT NOT NULL             COMMENT '交易前余额',
                                    balance_after   BIGINT NOT NULL             COMMENT '交易后余额',
                                    biz_type        VARCHAR(32) NOT NULL        COMMENT '业务类型（gift/live_room/recharge/...）',
                                    biz_id          VARCHAR(64) DEFAULT ''      COMMENT '业务单号',
                                    idempotent_key  VARCHAR(64) DEFAULT ''      COMMENT '幂等键（防重）',
                                    status          TINYINT NOT NULL DEFAULT 1  COMMENT '1=成功 2=已回退',
                                    remark          VARCHAR(256) DEFAULT ''     COMMENT '备注',
                                    create_time     BIGINT NOT NULL             COMMENT '创建时间',
                                    PRIMARY KEY (id),
                                    UNIQUE KEY uk_trans_no (trans_no),
                                    UNIQUE KEY uk_idempotent_key (idempotent_key),
                                    INDEX idx_user_time (user_id, create_time)
);
