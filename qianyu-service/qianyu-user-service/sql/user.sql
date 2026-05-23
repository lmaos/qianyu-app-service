
-- 在这个数据库创建
USE `qianyu`;

-- 用户授权表
DROP TABLE IF EXISTS `user_auth`;
CREATE TABLE IF NOT EXISTS  `user_auth` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物理主键(仅数据库行标识，分表可重复)',
                                            `user_id` bigint NOT NULL COMMENT '核心业务用户ID，关联用户主表',
                                            `identity_type` varchar(20) NOT NULL COMMENT '授权类型：phone/email/username/wechat/qq等',
    `identifier` varchar(191) NOT NULL COMMENT '授权标识：手机号/邮箱/用户名/三方openid',
    `credential` varchar(255) DEFAULT NULL COMMENT '凭证(密码/令牌，三方登录可为空)',
    `create_time` bigint DEFAULT NULL COMMENT '创建时间戳',
    `update_time` bigint DEFAULT NULL COMMENT '更新时间戳',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_identifier` (`identity_type`,`identifier`),
    KEY `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户多方式授权登录表';

-- 用户信息 主表
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE IF NOT EXISTS `user_info` (
                                           `user_id` bigint NOT NULL COMMENT '核心用户ID，业务主键。由分布式ID生成器生成，不自增。关联所有其他表（user_auth、user_identity等）',
                                           `user_no` varchar(64) NOT NULL COMMENT '用户外显ID，用于搜索、分享、展示。例如："U8a2F9c"。全局唯一，可让用户自定义或系统生成，建议6~20位字母数字组合',
    `nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称，可自定义，支持表情符号。最长64个字符，不唯一',
    `avatar` varchar(512) DEFAULT NULL COMMENT '用户头像URL。推荐使用CDN地址，支持https。可存OSS临时授权URL',
    `bio` varchar(1000) DEFAULT NULL COMMENT '个人简介/签名，最长1000字符。支持文字、表情、链接等',
    `gender` tinyint DEFAULT '0' COMMENT '性别：0=未知，1=女性，2=男性。由用户自行选择，非强制',
    `birthday` date DEFAULT NULL COMMENT '生日，格式 yyyy-MM-dd（如 1990-01-01）。用于年龄计算或星座展示，非必填',
    `age` tinyint DEFAULT '0' COMMENT '年龄（冗余字段，可由 birthday 实时计算更新）。便于按年龄筛选用户，每天/每周定时任务更新',
    `phone` varchar(50) DEFAULT NULL COMMENT '手机号（带国家码）。例如：+8613800000000。存储前需 AES-256 加密。该字段应加唯一索引（SQL 中未体现，建议 ALTER TABLE 添加），确保手机号与账号一一对应，符合实名制要求',
    `phone_verified_time` bigint DEFAULT '0' COMMENT '手机号认证通过时间戳（Unix 秒级）。0=未验证；>0 表示已通过短信或微信授权完成验证，是社交合规的最低要求',
    `email` varchar(128) DEFAULT NULL COMMENT '邮箱地址。用于登录、找回密码、接收通知。建议加唯一索引，存储前可加密（可选）',
    `country` varchar(3) DEFAULT NULL COMMENT '国家/地区，ISO 3166-1 alpha-2 两位代码，如 CN、US、DE。由客户端传递（GPS/用户选择），视为可修改的个人资料标签',
    `province` varchar(50) DEFAULT NULL COMMENT '省份/州。字符串形式，例如：北京市、California。自由文本，用于展示位置',
    `city` varchar(50) DEFAULT NULL COMMENT '城市。字符串形式，例如：朝阳区、Los Angeles。自由文本',
    `last_login_time` bigint DEFAULT '0' COMMENT '最后登录时间戳（Unix 秒级）。每次用户登录成功后更新，用于活跃度统计',
    `status` tinyint DEFAULT '0' COMMENT '账号状态总开关：0=正常，1=冻结封禁，2=注销。冻结时配合 freeze_end_time 使用；注销后账号不可恢复，所有关联数据应匿名化或删除',
    `freeze_end_time` bigint DEFAULT '0' COMMENT '冻结到期时间戳（Unix 秒级）。0=未冻结或永久冻结；>0 表示临时冻结到此时间点自动解冻。仅 status=1 时有效',
    `create_time` bigint DEFAULT NULL COMMENT '账号创建时间戳（Unix 秒级）。用户首次注册成功时记录',
    `update_time` bigint DEFAULT NULL COMMENT '账号信息最后更新时间戳（Unix 秒级）。每次修改 user_info 任何字段时更新',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_user_no` (`user_no`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社交项目-用户基础信息表（主表）';

-- 用户身份认证
DROP TABLE IF EXISTS `user_identity`;
CREATE TABLE IF NOT EXISTS `user_identity` (
                                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键，仅用于内部唯一标识，无业务含义',
                                               `user_id` bigint NOT NULL COMMENT '用户ID，关联 user_info.user_id，表示该实名认证属于哪个用户',
                                               `identity_type` tinyint NOT NULL COMMENT '证件类型：1=身份证（中国大陆居民），2=护照（国外用户或国际旅行证件），3=驾照（部分国家支持）',
                                               `full_name` varchar(100) NOT NULL COMMENT '证件上的完整姓名（必须与证件一致）。中国大陆身份证填中文姓名，护照填英文/当地文字姓名。存储前需使用 AES-256 加密',
    `id_number` varchar(100) NOT NULL COMMENT '证件号码（如身份证号、护照号、驾照编号）。存储前需使用 AES-256 加密',
    `country` varchar(3) DEFAULT NULL COMMENT '证件签发国/国籍，ISO 3166-1 alpha-2 两位代码。对于身份证/驾照表示签发国，对于护照表示国籍。由服务端根据证件信息识别后写入，不可由客户端直接传递',
    `expire_start` bigint DEFAULT NULL COMMENT '证件有效期起始时间戳（Unix 时间戳，秒级）。例如 1577836800 表示 2020-01-01 00:00:00 UTC。可为空表示证件长期有效（如某些老版身份证）',
    `expire_end` bigint DEFAULT NULL COMMENT '证件有效期结束时间戳（Unix 时间戳，秒级）。例如 1893427200 表示 2030-01-01 00:00:00 UTC。为空表示长期有效',
    `front_image` varchar(512) DEFAULT NULL COMMENT '证件正面照片的存储URL（推荐使用私有 OSS + 临时签名授权访问）。例如：https://your-oss.aliyuncs.com/identity/123_front.jpg?sign=xxx',
    `back_image` varchar(512) DEFAULT NULL COMMENT '证件反面照片URL（身份证反面、护照资料页等）。如证件仅一面（如某些国家驾照），此字段可为空',
    `status` tinyint NOT NULL DEFAULT '0' COMMENT '认证状态：0=未认证（用户已填信息但未提交或未核验），1=认证中（已提交，等待第三方核验），2=已认证（核验通过），3=认证失败（核验不通过，可查看 fail_reason）',
    `auth_time` bigint DEFAULT '0' COMMENT '认证通过时间戳（Unix 秒级）。当 status 变为 2 时，记录当前时间；未通过时保持 0',
    `fail_reason` varchar(255) DEFAULT NULL COMMENT '认证失败时的原因描述（仅 status=3 时有值），例如“姓名与身份证号不匹配”、“证件已过期”',
    `create_time` bigint NOT NULL COMMENT '记录创建时间戳（Unix 秒级），即用户首次提交认证资料的时间',
    `update_time` bigint NOT NULL COMMENT '记录最后更新时间戳（Unix 秒级），每次修改状态或资料时更新',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_type` (`user_id`, `identity_type`),
    KEY `idx_status` (`status`) COMMENT '便于后台查询待审核或失败记录'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户身份认证表（支持身份证/护照/驾照等证件实名）';