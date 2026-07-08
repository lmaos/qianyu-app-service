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
