-- ============================================================
-- 商户入驻 + 系统通知 模块 DDL（迁移脚本，2026-07-14）
-- 对应 docs/mall/merchant-onboarding/ 规划。可重复执行前请先核对列是否已存在。
-- ============================================================

-- (1) 系统通知（站内信）表 —— 新建
CREATE TABLE IF NOT EXISTS `msg_message` (
    `id`          BIGINT       NOT NULL                  COMMENT '主键（Snowflake）',
    `user_id`     BIGINT       NOT NULL                  COMMENT '接收用户ID',
    `type`        TINYINT      NOT NULL DEFAULT 1        COMMENT '1=商户 2=订单 3=支付 4=售后 5=系统',
    `title`       VARCHAR(128) NOT NULL                  COMMENT '标题',
    `content`     VARCHAR(512) NOT NULL                  COMMENT '正文（拒绝理由等）',
    `biz_type`    VARCHAR(32)  NOT NULL DEFAULT ''       COMMENT '业务类型(merchant_audit/order/pay/...) 用于跳转',
    `biz_id`      BIGINT       NOT NULL DEFAULT 0        COMMENT '业务ID(merchantId/orderId...) 用于跳转',
    `is_read`     TINYINT      NOT NULL DEFAULT 0        COMMENT '0=未读 1=已读',
    `read_time`   BIGINT       NOT NULL DEFAULT 0        COMMENT '已读时间(毫秒戳)',
    `create_time` BIGINT       NOT NULL                  COMMENT '创建时间(毫秒戳)',
    `update_time` BIGINT       NOT NULL DEFAULT 0        COMMENT '更新时间(毫秒戳)',
    `deleted`     TINYINT      NOT NULL DEFAULT 0        COMMENT '逻辑删除 0/1',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`),
    KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知(站内信)';

-- (2) mch_merchant 补资质字段（个体/企业、法人、邮箱、支行）——首次执行；已存在列会报错可忽略
ALTER TABLE `mch_merchant`
    ADD COLUMN `legal_person_name`    VARCHAR(64)  NULL COMMENT '法人/经营者姓名',
    ADD COLUMN `legal_person_id_card` VARCHAR(32)  NULL COMMENT '法人/经营者身份证号',
    ADD COLUMN `contact_email`        VARCHAR(128) NULL COMMENT '联系人邮箱',
    ADD COLUMN `bank_branch`          VARCHAR(128) NULL COMMENT '开户支行';

-- (3) 营业执照号唯一（业务层查重为主，DB 兜底；加索引前需清洗历史空/重复 license_no）
-- 若历史 license_no 存在空串或重复，先清洗：UPDATE mch_merchant SET license_no=NULL WHERE license_no='';
ALTER TABLE `mch_merchant` ADD UNIQUE KEY `uk_license_no` (`license_no`);
