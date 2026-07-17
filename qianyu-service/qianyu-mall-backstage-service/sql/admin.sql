-- ============================================================
-- 运营后台 RBAC + 审计表 DDL
-- 命名规范：t_admin_{}（用户拍板 2026-07-07）
-- 依据：docs/mall/backstage/02-tech-design.md 五、数据模型
--       docs/mall/backstage/04-update-20260707-auth-sql.md
-- 字段约定：
--   - 时间 BIGINT 毫秒戳（create_time/update_time，与现网 mall 表一致，非 created_at）
--   - 主键雪花手动赋值（@Id(keyType=KeyType.None)）
--   - 逻辑删除 deleted TINYINT 0/1（关联表/日志表无 deleted）
-- ============================================================

-- (1) 运营账号
CREATE TABLE t_admin_account (
    id            BIGINT       NOT NULL                COMMENT '雪花ID',
    username      VARCHAR(64)  NOT NULL                COMMENT '登录名(唯一)',
    pwd_hash      VARCHAR(128) NOT NULL                COMMENT '密码哈希(BCrypt cost≥12)',
    pwd_salt      VARCHAR(64)  NOT NULL                COMMENT '盐',
    real_name     VARCHAR(64)                          COMMENT '真实姓名',
    mobile        VARCHAR(20)                          COMMENT '手机',
    email         VARCHAR(128)                         COMMENT '邮箱',
    status        TINYINT      NOT NULL DEFAULT 1      COMMENT '1启用 0禁用 2冻结',
    last_login_at BIGINT                               COMMENT '最近登录时间(毫秒戳)',
    last_login_ip VARCHAR(64)                          COMMENT '最近登录IP',
    fail_count    INT          NOT NULL DEFAULT 0      COMMENT '连续失败次数(达5次锁15min)',
    create_time   BIGINT       NOT NULL                COMMENT '创建时间(毫秒戳)',
    update_time   BIGINT       NOT NULL                COMMENT '更新时间(毫秒戳)',
    deleted       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除 0/1',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) COMMENT='运营账号';

-- (2) 角色
CREATE TABLE t_admin_role (
    id           BIGINT       NOT NULL,
    role_code    VARCHAR(64)  NOT NULL                COMMENT '角色码',
    role_name    VARCHAR(64)  NOT NULL                COMMENT '角色名',
    status       TINYINT      NOT NULL DEFAULT 1      COMMENT '1启用 0禁用',
    remark       VARCHAR(255)                         COMMENT '备注',
    create_time  BIGINT       NOT NULL,
    update_time  BIGINT       NOT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) COMMENT='运营角色';

-- (3) 权限点
CREATE TABLE t_admin_permission (
    id           BIGINT       NOT NULL,
    perm_code    VARCHAR(128) NOT NULL                COMMENT '权限码(M0冻结字典, 如 mch:audit)',
    perm_name    VARCHAR(128) NOT NULL                COMMENT '权限名',
    type         TINYINT      NOT NULL                COMMENT '1菜单 2按钮 3接口',
    parent_id    BIGINT       NOT NULL DEFAULT 0      COMMENT '父权限ID',
    path         VARCHAR(255)                         COMMENT '菜单/路由路径',
    method       VARCHAR(64)                          COMMENT '接口 method(接口型)',
    create_time  BIGINT       NOT NULL,
    update_time  BIGINT       NOT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code),
    KEY idx_parent_id (parent_id)
) COMMENT='运营权限点';

-- (4) 账号-角色关联(无 deleted)
CREATE TABLE t_admin_account_role (
    account_id   BIGINT       NOT NULL,
    role_id      BIGINT       NOT NULL,
    create_time  BIGINT       NOT NULL,
    PRIMARY KEY (account_id, role_id),
    KEY idx_role_id (role_id)
) COMMENT='账号-角色关联';

-- (5) 角色-权限关联(无 deleted)
CREATE TABLE t_admin_role_permission (
    role_id       BIGINT      NOT NULL,
    permission_id BIGINT      NOT NULL,
    create_time   BIGINT      NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    KEY idx_permission_id (permission_id)
) COMMENT='角色-权限关联';

-- (6) 登录日志(无 deleted, 只增)
CREATE TABLE t_admin_login_log (
    id           BIGINT       NOT NULL,
    account_id   BIGINT                                COMMENT '账号ID(登录失败可能无)',
    username     VARCHAR(64)  NOT NULL,
    login_at     BIGINT       NOT NULL                 COMMENT '登录时间(毫秒戳)',
    login_ip     VARCHAR(64),
    user_agent   VARCHAR(512),
    result       TINYINT      NOT NULL                 COMMENT '1成功 0失败',
    fail_reason  VARCHAR(255),
    create_time  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    KEY idx_account_id (account_id),
    KEY idx_login_at (login_at)
) COMMENT='运营登录日志';

-- (7) 操作日志(无 deleted, 只增, 审计刚性)
CREATE TABLE t_admin_op_log (
    id            BIGINT      NOT NULL,
    account_id    BIGINT      NOT NULL,
    username      VARCHAR(64) NOT NULL,
    perm_code     VARCHAR(128)                         COMMENT '权限码',
    target_entity VARCHAR(64)                          COMMENT '操作实体(如 MerchantWithdrawal)',
    target_id     VARCHAR(64)                          COMMENT '操作目标ID',
    before_json   TEXT                                 COMMENT '前态快照JSON(资金/资质类必填)',
    after_json    TEXT                                 COMMENT '后态快照JSON',
    ip            VARCHAR(64),
    user_agent    VARCHAR(512),
    ts            BIGINT      NOT NULL                 COMMENT '操作时间(毫秒戳)',
    result        TINYINT      NOT NULL                COMMENT '1成功 0失败',
    cost_ms       INT                                  COMMENT '耗时(ms)',
    err_msg       VARCHAR(512),
    create_time   BIGINT      NOT NULL,
    PRIMARY KEY (id),
    KEY idx_account_id (account_id),
    KEY idx_ts (ts),
    KEY idx_perm_code (perm_code)
) COMMENT='运营操作日志';

-- ============================================================
-- 种子数据（BG-05a，幂等，可重复执行）
--   - super_admin（admin/admin123）：拥有全部 21 个 permCode，供 BG-06 全链路冒烟登录。
--   - viewer（auditor/admin123）：仅 7 个 *:view 权限，缺 mch:withdrawal:approve 等，供 BG-06#11 / BG-07 BS-16 无权限 403 用例。
--   - 密码 BCrypt cost=12（与 AdminAccountViewServiceBizImpl.PASSWORD_ENCODER 一致），明文 admin123。
--     哈希由 spring-security-crypto BCryptPasswordEncoder(12).encode("admin123") 生成（salt 内嵌，可重复验证 matches=true）。
--   - 幂等：实体表 ON DUPLICATE KEY UPDATE id=id（命中 uk_username/uk_role_code/uk_perm_code 或主键）；
--     关联表（复合主键）用 INSERT IGNORE。
--   - 仅首次执行需真正写入；已存在的行保持不变。
-- ============================================================

-- (a) 权限点（21 个，permCode 字典对齐 @RequiresPermission 实际取值）
INSERT INTO t_admin_permission (id, perm_code, perm_name, type, parent_id, path, method, create_time, update_time, deleted) VALUES
  (101, 'admin:account:manage',     '账号管理',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (102, 'admin:role:manage',        '角色管理',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (103, 'admin:permission:manage',  '权限管理',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (104, 'admin:oplog:view',         '操作日志查看', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (105, 'mch:audit',                '商户审核',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (106, 'mch:merchant:view',        '商户查看',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (107, 'mch:merchant:freeze',      '商户冻结/解冻',3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (108, 'mch:merchant:disable',     '商户禁用',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (109, 'mch:withdrawal:view',      '提现查看',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (110, 'mch:withdrawal:approve',   '提现审批通过', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (111, 'mch:withdrawal:reject',    '提现拒绝',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (112, 'mch:withdrawal:transfer',  '提现打款标记', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (113, 'pms:spu:view',             '商品查看',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (114, 'pms:spu:listoff',          '商品上下架',   3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (115, 'pms:spu:audit',            '商品审核',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (116, 'oms:order:view',           '订单查看',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (117, 'oms:order:close',          '订单关闭',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (118, 'oms:aftersale:view',       '售后查看',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (119, 'oms:aftersale:arbitrate',  '售后仲裁',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (120, 'rev:review:view',          '评价查看',     3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (121, 'rev:review:batchUpdate',   '评价批量处理', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0)
ON DUPLICATE KEY UPDATE id = id;

-- (b) 角色（super_admin 全权限 / viewer 只读）
INSERT INTO t_admin_role (id, role_code, role_name, status, remark, create_time, update_time, deleted) VALUES
  (1, 'super_admin', '超级管理员', 1, '全部权限（BG-05a 种子）', 1719907200000, 1719907200000, 0),
  (2, 'viewer',      '只读运营',   1, '仅 *:view 权限（BG-06#11/BG-07 BS-16 无权限用例）', 1719907200000, 1719907200000, 0)
ON DUPLICATE KEY UPDATE id = id;

-- (c) 角色-权限关联
-- super_admin(1) → 全部 21 个权限
INSERT IGNORE INTO t_admin_role_permission (role_id, permission_id, create_time) VALUES
  (1, 101, 1719907200000), (1, 102, 1719907200000), (1, 103, 1719907200000), (1, 104, 1719907200000),
  (1, 105, 1719907200000), (1, 106, 1719907200000), (1, 107, 1719907200000), (1, 108, 1719907200000),
  (1, 109, 1719907200000), (1, 110, 1719907200000), (1, 111, 1719907200000), (1, 112, 1719907200000),
  (1, 113, 1719907200000), (1, 114, 1719907200000), (1, 115, 1719907200000), (1, 116, 1719907200000),
  (1, 117, 1719907200000), (1, 118, 1719907200000), (1, 119, 1719907200000), (1, 120, 1719907200000),
  (1, 121, 1719907200000);
-- viewer(2) → 仅 7 个 *:view 权限（无 approve/reject/transfer/audit/manage）
INSERT IGNORE INTO t_admin_role_permission (role_id, permission_id, create_time) VALUES
  (2, 104, 1719907200000), (2, 106, 1719907200000), (2, 109, 1719907200000),
  (2, 113, 1719907200000), (2, 116, 1719907200000), (2, 118, 1719907200000), (2, 120, 1719907200000);

-- (d) 运营账号（密码 admin123，BCrypt cost=12；pwd_salt 留空串，BCrypt 自带盐）
INSERT INTO t_admin_account (id, username, pwd_hash, pwd_salt, real_name, status, fail_count, create_time, update_time, deleted) VALUES
  (1, 'admin',   '$2a$12$6ag3qfSu1egjD2cr03H4tOiOpVBTk2wK75fGPP6aJWYQtFIbugks6', '', '超级管理员', 1, 0, 1719907200000, 1719907200000, 0),
  (2, 'auditor', '$2a$12$6ag3qfSu1egjD2cr03H4tOiOpVBTk2wK75fGPP6aJWYQtFIbugks6', '', '只读运营',   1, 0, 1719907200000, 1719907200000, 0)
ON DUPLICATE KEY UPDATE id = id;

-- (e) 账号-角色关联
INSERT IGNORE INTO t_admin_account_role (account_id, role_id, create_time) VALUES
  (1, 1, 1719907200000),  -- admin → super_admin
  (2, 2, 1719907200000);  -- auditor → viewer

-- =============================================================================
-- (追加 2026-07-16) 分类管理权限点 pms:category:* （后台分类管理页 PmsAdminCategoryController）
--   续号 122-125；super_admin 全挂，viewer 仅 :view。幂等（ON DUPLICATE / IGNORE）。
-- =============================================================================
INSERT INTO t_admin_permission (id, perm_code, perm_name, type, parent_id, path, method, create_time, update_time, deleted) VALUES
  (122, 'pms:category:view',   '分类查看', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (123, 'pms:category:create', '分类创建', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (124, 'pms:category:update', '分类编辑', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (125, 'pms:category:delete', '分类删除', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0)
ON DUPLICATE KEY UPDATE id = id;

-- super_admin(1) → 分类全部 4 个权限
INSERT IGNORE INTO t_admin_role_permission (role_id, permission_id, create_time) VALUES
  (1, 122, 1719907200000), (1, 123, 1719907200000), (1, 124, 1719907200000), (1, 125, 1719907200000);
-- viewer(2) → 仅分类查看
INSERT IGNORE INTO t_admin_role_permission (role_id, permission_id, create_time) VALUES
  (2, 122, 1719907200000);

-- =============================================================================
-- (追加 2026-07-16) CMS 楼层管理权限点 cms:zone:* （CmsAdminZoneController）
--   续号 126-127；super_admin 全挂，viewer 仅 :view。幂等。
-- =============================================================================
INSERT INTO t_admin_permission (id, perm_code, perm_name, type, parent_id, path, method, create_time, update_time, deleted) VALUES
  (126, 'cms:zone:view',   '楼层查看', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (127, 'cms:zone:manage', '楼层管理', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0)
ON DUPLICATE KEY UPDATE id = id;

-- =============================================================================
-- (追加 2026-07-16) CMS Tab/Banner 管理权限点（CmsAdminTabController/CmsAdminBannerController）
--   续号 128-129；super_admin 全挂。幂等。
-- =============================================================================
INSERT INTO t_admin_permission (id, perm_code, perm_name, type, parent_id, path, method, create_time, update_time, deleted) VALUES
  (128, 'cms:tab:manage',    '导航Tab管理', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (129, 'cms:banner:manage', '轮播Banner管理', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0)
ON DUPLICATE KEY UPDATE id = id;

-- =============================================================================
-- (追加 2026-07-17) 优惠券管理权限点（AdminCouponController）
--   续号 130-134；super_admin(线上 178342042368026174) 全挂，viewer(2) 仅 view。
-- =============================================================================
INSERT INTO t_admin_permission (id, perm_code, perm_name, type, parent_id, path, method, create_time, update_time, deleted) VALUES
  (130, 'sms:coupon:view',   '优惠券查看', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (131, 'sms:coupon:create', '优惠券创建', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (132, 'sms:coupon:update', '优惠券编辑', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (133, 'sms:coupon:delete', '优惠券删除', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0),
  (134, 'sms:coupon:manage', '优惠券启停', 3, 0, NULL, NULL, 1719907200000, 1719907200000, 0)
ON DUPLICATE KEY UPDATE id = id;

-- super_admin 全挂（线上 Snowflake 角色 ID）
INSERT IGNORE INTO t_admin_role_permission (role_id, permission_id) VALUES
  (178342042368026174, 130),
  (178342042368026174, 131),
  (178342042368026174, 132),
  (178342042368026174, 133),
  (178342042368026174, 134);
-- viewer 仅 view
INSERT IGNORE INTO t_admin_role_permission (role_id, permission_id) VALUES
  (2, 130);
