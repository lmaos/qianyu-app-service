-- ============================================================
-- 千语商城 (QianYu Mall) 数据库初始化脚本
-- ============================================================
-- 数据库:  qianyu
-- 字符集:  utf8mb4 / utf8mb4_unicode_ci
-- 存储引擎: InnoDB
-- 单库单表: 本脚本不展开分表（每张逻辑表 1 张物理表）
-- 表数量:  41 张（32 张来自 ddl/ 目录，9 张来自汇总文档）
-- 生成时间: 2026-06-06
--
-- 来源:
--   docs/mall/ddl/*/*.sql  (32 张 — 权威源)
--   docs/mall/千语商城 -- 数据库表结构汇总.md  (9 张补充: cms_* 3, mch_settlement 1, sms_* 5)
--
-- 分片策略（参考，Phase 1 不展开）:
--   user_id × 16: oms_cart / oms_order / oms_order_item / oms_after_sale
--                 pay_payment / pay_refund / sms_user_coupon / sms_user_coupon_log
--                 log_shipping / log_delivery_trace / mch_bill / mch_settlement
--                 mch_withdrawal
--   merchant_id × 16: mch_bill / mch_settlement / mch_withdrawal
--   order_id × 16:   oms_order_item / pay_refund
--   sku_id × 8:      inv_stock / inv_stock_log
--   spu_id × 8:      rev_review
--   shipping_id × 16: log_delivery_trace
--   user_id × 8:     ads_address / fav_favorite / his_browse_history
--
-- 执行方式:
--   mysql -u root -p < mall.sql
-- ============================================================

-- 创建并切换到 qianyu 库
CREATE DATABASE IF NOT EXISTS `qianyu` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `qianyu`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- PMS 商品管理  (6 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 商品 - 商品属性/规格定义表
-- ============================================================
-- 业务说明: 商品属性/规格定义表，定义分类下的属性模板。type=1 为销售属性（如颜色、尺码），
--          用于生成 SKU 组合；type=2 为商品参数（如材质、产地），用于详情展示。
-- 分片策略: 不分片
-- 分片键  : 无
-- 预估量级: 万级（~20000 条）
-- ============================================================



CREATE TABLE IF NOT EXISTS `pms_attribute` (
    `id`          BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `category_id` BIGINT       NOT NULL COMMENT '所属分类ID',
    `name`        VARCHAR(64)  NOT NULL COMMENT '属性名称（如 color、材质）',
    `type`        TINYINT      NOT NULL COMMENT '属性类型: 1=销售属性, 2=商品参数',
    `input_type`  TINYINT      NOT NULL DEFAULT 1 COMMENT '录入方式: 1=手工录入, 2=列表选择',
    `values`      JSON         NULL COMMENT '可选值列表, 格式: ["红色","蓝色","黑色"]',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `create_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性/规格定义表-第三级类别包含的属性模板';

-- ============================================================
-- 千语商城 - 商品 - 品牌表
-- ============================================================
-- 业务说明: 品牌表，管理商品品牌信息，SPU 关联品牌用于品牌筛选和展示。
-- 分片策略: 不分片
-- 分片键  : 无
-- 预估量级: 万级（~50000 条）
-- ============================================================



CREATE TABLE IF NOT EXISTS `pms_brand` (
    `id`          BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `name`        VARCHAR(128) NOT NULL COMMENT '品牌名称',
    `logo`        VARCHAR(255) NULL COMMENT '品牌Logo URL',
    `description` VARCHAR(512) NULL COMMENT '品牌描述',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=显示, 1=隐藏',
    `create_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

-- ============================================================
-- 千语商城 - 商品 - 商品分类表
-- ============================================================
-- 业务说明: 商品分类表，支持无限层级分类树，通过 parent_id + path 双模式实现。
--           parent_id 用于加载直接子分类；path（物化路径）用于查完整子树、面包屑、移动子树，无需递归 CTE。
-- 分片策略: 不分片
-- 分片键  : 无
-- 预估量级: 千级（~5000 条）
-- ============================================================



CREATE TABLE IF NOT EXISTS `pms_category` (
    `id`          BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID，顶级分类为0',
    `path`        VARCHAR(255) NOT NULL COMMENT '物化路径，从根到当前节点，格式: "1/5/12"，顶级为自身ID',
    `img_id`      VARCHAR(255) NOT NULL COMMENT '图片地址',
    `name`        VARCHAR(64)  NOT NULL COMMENT '分类名称',
    `level`       TINYINT      NOT NULL COMMENT '分类层级（由 path 中 / 数量自动推导）: 1=一级, 2=二级, 3=三级...',
    `icon`        VARCHAR(255) NULL COMMENT '分类图标URL',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '同级别排序值，越小越靠前',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=显示, 1=隐藏',
    `create_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name_parent` (`name`, `parent_id`, `deleted`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表（物化路径模型）';

-- ============================================================
-- 千语商城 - 商品 - SKU 库存单元表
-- ============================================================
-- 业务说明: SKU 库存单元表，表示 SPU 下的最小可售卖单元，定义具体规格组合、价格和库存。
--          用户下单以 SKU 为粒度。每个 SPU 下有且仅有一个默认 SKU（is_default=1），
--          商品详情页默认选中该 SKU；若用户从列表页点击进入则使用默认 SKU，
--          若从购物车/订单等场景进入则使用指定 skuId。
--          sku_name 和 sku_image 用于购物车/订单快照展示，避免回查 SPU。
--          库存由 inv_stock 表统一管理，本表不含冗余库存字段。
-- 分片策略: Phase 1 不分片，Phase 2+ 按 spu_id 分片（与 SPU 同库 JOIN）
-- 分片键  : spu_id（Phase 2+）
-- 预估量级: 千万级（~50000000 条）
-- ============================================================



CREATE TABLE IF NOT EXISTS `pms_sku` (
    `id`              BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`   BIGINT       NOT NULL COMMENT '商户ID',
    `spu_id`          BIGINT       NOT NULL COMMENT '所属SPU ID',
    `sku_code`        VARCHAR(64)  NOT NULL COMMENT 'SKU编码（商户自定义或系统生成）',
    `barcode`         VARCHAR(64)  DEFAULT NULL COMMENT '商品条形码（EAN-13/UPC-A，用于扫码和ERP对接）',
    `attributes`      JSON         NULL COMMENT '销售属性, 格式: [{"k":"颜色","v":"红色"},{"k":"尺码","v":"XL"}]',
    `sku_name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT 'SKU名称（如"红色-XL"），用于购物车/订单快照展示，避免前端拼接',
    `sku_image`       VARCHAR(255) DEFAULT NULL COMMENT 'SKU主图URL（不同规格展示不同图片），NULL时回退到SPU主图',
    `price`           DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '销售价格（单位: 元）',
    `original_price`  DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '原价/划线价（单位: 元）',
    `cost_price`      DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '成本价（单位: 元）',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=上架, 1=下架',
    `is_default`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认SKU: 0=否, 1=是（每个SPU最多一个默认SKU，商品详情页默认选中）',
    `weight`          DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '重量（千克），用于按重量计费运费模板',
    `volume`          DECIMAL(10,6) NOT NULL DEFAULT 0.000000 COMMENT '体积（立方米），用于按体积计费运费模板',
    `create_time`     BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_spu_default` (`spu_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU 库存单元表';

-- ============================================================
-- 千语商城 - 商品 - SPU 标准商品单元表
-- ============================================================
-- 业务说明: SPU 标准商品单元表，表示一类商品的标准化信息（名称、图片、描述等），
--          一个 SPU 可包含多个 SKU（不同规格/价格/库存）。由商户创建并管理，归属到具体店铺。
--          搜索相关冗余字段（min_price、comment_count、avg_score）用于列表页排序和 ES 同步，由异步任务更新。
-- 分片策略: Phase 1 不分片，Phase 2+ 按 merchant_id 分片
-- 分片键  : merchant_id（Phase 2+）
-- 预估量级: 百万级（~5000000 条）
-- ============================================================



CREATE TABLE IF NOT EXISTS `pms_spu` (
    `id`            BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`   BIGINT        NOT NULL COMMENT '商户ID',
    `store_id`      BIGINT        NOT NULL COMMENT '所属店铺ID（关联 mch_store.id）',
    `brand_id`      BIGINT        NULL COMMENT '品牌ID',
    `category_id`   BIGINT        NULL COMMENT '主分类ID（冗余字段，关联 pms_category.id，用于分类筛选避免 JOIN）',
    `name`          VARCHAR(256)  NOT NULL COMMENT '商品名称',
    `subtitle`      VARCHAR(512)  NULL COMMENT '商品副标题',
    `main_image`    VARCHAR(255)  NULL COMMENT '主图URL',
    `thumb_image`   VARCHAR(255)  DEFAULT NULL COMMENT '缩略图URL（列表页使用，避免加载原图）',
    `images`        JSON          NULL COMMENT '商品图片列表, 格式: ["url1","url2"]',
    `description`   TEXT          NULL COMMENT '商品详情（富文本/HTML）',
    `keywords`      VARCHAR(512)  DEFAULT NULL COMMENT '搜索关键词（逗号分隔），用于搜索召回',
    `unit`          VARCHAR(16)   NOT NULL DEFAULT '个' COMMENT '计量单位（个/件/箱等）',
    `status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿, 1=上架, 2=下架, 3=已删除',
    `sort`                INT           NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `freight_template_id` BIGINT        DEFAULT NULL COMMENT '运费模板ID（关联 mch_freight_template.id），NULL=免运费',
    `min_price`           DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT 'SKU 最低价（元），冗余字段用于搜索价格区间筛选和列表展示',
    `sales`         INT           NOT NULL DEFAULT 0 COMMENT '累计销量（由订单完成后异步更新）',
    `comment_count` INT           NOT NULL DEFAULT 0 COMMENT '累计评价数（由评价写入时异步更新）',
    `avg_score`     DECIMAL(2,1)  NOT NULL DEFAULT 0.0 COMMENT '平均评分（1.0~5.0），由评价写入时异步更新',
    `publish_time`  BIGINT        NOT NULL DEFAULT 0 COMMENT '最近上架时间（毫秒时间戳）',
    `create_time`   BIGINT        NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`   BIGINT        NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_store_id` (`store_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU 标准商品单元表';

-- ============================================================
-- 千语商城 - 商品 - SPU-分类关联表
-- ============================================================
-- 业务说明: SPU-分类关联表，一个 SPU 可归属于多个分类（如某商品同时属于"手机"和"数码"分类），
--          用于分类筛选和搜索。
-- 分片策略: Phase 1 不分片，Phase 2+ 按 spu_id 分片
-- 分片键  : spu_id（Phase 2+）
-- 预估量级: 百万级（~8000000 条）
-- ============================================================



CREATE TABLE IF NOT EXISTS `pms_spu_category` (
    `id`          BIGINT NOT NULL COMMENT '主键（Snowflake ID）',
    `spu_id`      BIGINT NOT NULL COMMENT 'SPU ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `create_time` BIGINT NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_category` (`spu_id`, `category_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU-分类关联表';

-- ------------------------------------------------------------
-- OMS 订单管理  (4 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 订单 - 售后申请表
-- ============================================================
-- 业务说明: 存储用户提交的售后申请单，支持仅退款、退货退款、换货、维修四种类型。
--           退货退款和换货场景下，用户需要在商家同意后填写退货物流信息；
--           换货和维修场景下，商家需要填写寄回物流信息。
-- 分片策略: 按 user_id 分 16 表
-- 分片键  : user_id
-- 预估量级: 单表约 300 万行
-- ============================================================



CREATE TABLE IF NOT EXISTS `oms_after_sale` (
    `id`                      BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `after_sale_no`           VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '售后单号（唯一，供用户和客服引用）',
    `order_id`                BIGINT        NOT NULL COMMENT '关联订单 ID',
    `order_item_id`           BIGINT        NOT NULL COMMENT '关联订单明细 ID',
    `user_id`                 BIGINT        NOT NULL COMMENT '申请人用户 ID',
    `merchant_id`             BIGINT        NOT NULL COMMENT '商家 ID',
    `type`                    TINYINT       NOT NULL COMMENT '售后类型: 1=仅退款, 2=退货退款, 3=换货, 4=维修',
    `reason`                  VARCHAR(256)  NOT NULL DEFAULT '' COMMENT '售后原因',
    `description`             VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '问题描述',
    `amount`                  DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '退款金额（元）',
    `images`                  JSON          DEFAULT NULL COMMENT '凭证图片列表, 格式: ["url1","url2"]',
    `status`                  TINYINT       NOT NULL DEFAULT 10 COMMENT '售后状态: 10=待审核, 20=商家同意, 30=商家拒绝, 40=用户已发货, 50=已完成, 60=已取消',
    `reject_reason`           VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '商家拒绝原因',
    `return_shipping_no`      VARCHAR(64)   DEFAULT NULL COMMENT '退货物流单号（用户填写）',
    `return_shipping_company` VARCHAR(64)   DEFAULT NULL COMMENT '退货物流公司编码（用户填写）',
    `send_back_shipping_no`   VARCHAR(64)   DEFAULT NULL COMMENT '商家寄回物流单号（换货/维修场景，商家填写）',
    `send_back_shipping_company` VARCHAR(64) DEFAULT NULL COMMENT '商家寄回物流公司编码（换货/维修场景，商家填写）',
    `refund_time`             BIGINT        NOT NULL DEFAULT 0 COMMENT '退款完成时间（毫秒时间戳）',
    `create_time`             BIGINT        NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`             BIGINT        NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`                 TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_after_sale_no` (`after_sale_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_merchant_status` (`merchant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后申请表';

-- ============================================================
-- 千语商城 - 订单 - 购物车表
-- ============================================================
-- 业务说明: 存储用户购物车中的商品条目，记录用户选择的 SPU/SKU 及数量、勾选状态。
--           商品名称和图片做快照，确保 SKU 下架后购物车仍可展示。实际结算价格以 SKU 实时价格为准。
-- 分片策略: 按 user_id 分 16 表
-- 分片键  : user_id
-- 预估量级: 单表约 500 万行
-- ============================================================



CREATE TABLE IF NOT EXISTS `oms_cart` (
    `id`          BIGINT    NOT NULL COMMENT '主键（Snowflake ID）',
    `user_id`     BIGINT    NOT NULL COMMENT '用户 ID',
    `merchant_id` BIGINT    NOT NULL DEFAULT 0 COMMENT '商家ID（用于购物车按店铺分组）',
    `spu_id`      BIGINT    NOT NULL COMMENT 'SPU ID',
    `sku_id`      BIGINT    NOT NULL COMMENT 'SKU ID',
    `sku_name`    VARCHAR(256) NOT NULL DEFAULT '' COMMENT 'SKU名称快照（商品下架后购物车仍可展示）',
    `sku_image`   VARCHAR(512) NOT NULL DEFAULT '' COMMENT 'SKU主图URL快照',
    `quantity`    INT       NOT NULL DEFAULT 1 COMMENT '商品数量',
    `checked`     TINYINT   NOT NULL DEFAULT 1 COMMENT '勾选状态: 0=未勾选, 1=已勾选',
    `create_time` BIGINT    NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT    NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`     TINYINT   NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ============================================================
-- 千语商城 - 订单 - 订单主表
-- ============================================================
-- 业务说明: 存储用户订单的核心信息。一个订单只属于一个商家（购物车结算时按商家拆单）。
--           订单状态依次流转：待付款(10)->待发货(20)->已发货(30)->已完成(40)；
--           也可从待付款取消(50)或超时关闭(60)。
-- 分片策略: 按 user_id 分 16 表
-- 分片键  : user_id
-- 预估量级: 单表约 3000 万行
-- ============================================================



CREATE TABLE IF NOT EXISTS `oms_order` (
    `id`               BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `order_no`         VARCHAR(32)  NOT NULL COMMENT '订单编号',
    `user_id`          BIGINT       NOT NULL COMMENT '买家用户 ID',
    `merchant_id`      BIGINT       NOT NULL COMMENT '商家 ID',
    `total_amount`     DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '订单总金额（元）',
    `pay_amount`       DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '实付金额（元）',
    `freight_amount`   DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '运费金额（元）',
    `coupon_amount`    DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '优惠券抵扣金额（元）',
    `coupon_user_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '使用的用户优惠券ID（关联 sms_user_coupon.id），0=未使用优惠券',
    `discount_amount`  DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '促销优惠金额（元），包含满减/打折/秒杀等非优惠券类优惠',
    `discount_detail`  JSON         DEFAULT NULL COMMENT '优惠明细, 格式: [{"type":"promotion","id":1001,"name":"满100减20","amount":"20.00"},{"type":"flash_sale","id":2001,"name":"秒杀直降10","amount":"10.00"},{"type":"coupon","id":6001,"name":"新人券","amount":"10.00"}]',
    `total_quantity`   INT           NOT NULL DEFAULT 0 COMMENT '订单总商品件数',
    `status`           TINYINT      NOT NULL DEFAULT 10 COMMENT '订单状态: 10=待付款, 20=待发货, 30=已发货, 40=已完成, 50=已取消, 60=已关闭',
    `after_sale_status` TINYINT     NOT NULL DEFAULT 0 COMMENT '售后状态: 0=无售后 1=售后中 2=售后成功 3=售后拒绝/取消（冗余字段，售后单变更时同步更新，订单列表单表查询无需 JOIN）',
    `after_sale_type`  TINYINT      NOT NULL DEFAULT 0 COMMENT '售后类型: 0=无 1=仅退款 2=退货退款 3=换货 4=维修（冗余字段，与 after_sale_status 同步更新，用于前端精确展示"已退款/已换货/已维修"）',
    `version`          BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，每次更新 +1',
    `source`           TINYINT      NOT NULL DEFAULT 1 COMMENT '订单来源：1=APP 2=H5 3=微信小程序 4=直播间',
    `buyer_ip`         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '下单客户端IP（风控+第三方支付必传参数）',
    `buyer_message`    VARCHAR(512) NOT NULL DEFAULT '' COMMENT '买家留言',
    `merchant_remark`  VARCHAR(512) NOT NULL DEFAULT '' COMMENT '商家内部备注（用户不可见）',
    `address_snapshot` JSON         DEFAULT NULL COMMENT '收货地址快照, 格式: {"name":"张三","phone":"1380000","province":"北京","city":"北京市","district":"朝阳区","detail":"xxx路"}',
    `pay_time`         BIGINT       NOT NULL DEFAULT 0 COMMENT '支付时间（毫秒时间戳）',
    `delivery_time`    BIGINT       NOT NULL DEFAULT 0 COMMENT '发货时间（毫秒时间戳）',
    `receive_time`     BIGINT       NOT NULL DEFAULT 0 COMMENT '确认收货时间（毫秒时间戳）',
    `close_time`       BIGINT       NOT NULL DEFAULT 0 COMMENT '关闭时间（毫秒时间戳）',
    `create_time`      BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`      BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_merchant_status` (`merchant_id`, `status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- ============================================================
-- 千语商城 - 订单 - 订单明细表
-- ============================================================
-- 业务说明: 存储订单中的商品明细，每条记录对应一个 SKU 行项目。包含商品快照信息
--           （名称、图片、销售属性），确保订单历史数据不受后续商品变更影响。
--           本表随订单生命周期管理，不设逻辑删除字段。
-- 分片策略: 按 order_id 分 16 表（与订单同库）
-- 分片键  : order_id
-- 预估量级: 单表约 6000 万行
-- ============================================================



CREATE TABLE IF NOT EXISTS `oms_order_item` (
    `id`           BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `order_id`     BIGINT        NOT NULL COMMENT '订单 ID',
    `merchant_id`  BIGINT        NOT NULL DEFAULT 0 COMMENT '商家ID',
    `spu_id`       BIGINT        NOT NULL COMMENT 'SPU ID',
    `sku_id`       BIGINT        NOT NULL COMMENT 'SKU ID',
    `sku_name`     VARCHAR(256)  NOT NULL DEFAULT '' COMMENT 'SKU 名称',
    `sku_image`    VARCHAR(512)  NOT NULL DEFAULT '' COMMENT 'SKU 主图 URL',
    `price`        DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '商品单价（元）',
    `quantity`     INT           NOT NULL DEFAULT 1 COMMENT '购买数量',
    `total_amount` DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '行项目总金额（元）',
    `attributes`   JSON          DEFAULT NULL COMMENT 'SKU 销售属性快照, 格式: [{"k":"颜色","v":"红色"}]',
    `create_time`  BIGINT        NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`  BIGINT        NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ------------------------------------------------------------
-- PAY 支付  (2 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 支付 - 支付记录表
-- ============================================================
-- 业务说明: 支付记录表 —— 记录用户每笔订单的支付流水。pay_channel 区分支付渠道（微信/支付宝/余额），
--           pay_type 进一步区分具体支付方式（微信 JSAPI/APP/H5/Native 等），用于调用不同的第三方支付接口。
--           callback_status 用于回调幂等控制，防止重复处理。
-- 分片策略: 按 user_id 分 16 表
-- 分片键  : user_id
-- 预估量级: 日均 50 万笔，保留 1 年 约 1.8 亿行
-- ============================================================



CREATE TABLE IF NOT EXISTS `pay_payment` (
    `id`              BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `payment_no`      VARCHAR(32)  NOT NULL COMMENT '支付单号',
    `order_id`        BIGINT       NOT NULL COMMENT '关联订单 ID',
    `user_id`         BIGINT       NOT NULL COMMENT '支付用户 ID',
    `merchant_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '收款商家ID',
    `amount`          DECIMAL(20,6) NOT NULL COMMENT '支付金额（元）',
    `pay_channel`     TINYINT      NOT NULL COMMENT '支付渠道：1=微信支付 2=支付宝 3=余额支付',
    `pay_type`        TINYINT      NOT NULL DEFAULT 0 COMMENT '支付方式：1=微信JSAPI(小程序) 2=微信APP 3=微信H5 4=微信Native(扫码) 5=支付宝APP 6=支付宝H5 7=支付宝网页 8=余额支付',
    `third_pay_uid`   VARCHAR(64)  DEFAULT NULL COMMENT '第三方支付用户标识（微信openid/支付宝buyer_id，JSAPI支付必传）',
    `buyer_ip`        VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '支付客户端IP（第三方支付API必传参数，风控溯源）',
    `pay_status`      TINYINT      NOT NULL DEFAULT 10 COMMENT '支付状态：10=待支付 20=支付成功 30=支付失败 40=已关闭',
    `transaction_id`  VARCHAR(128)  DEFAULT NULL COMMENT '第三方交易号（微信/支付宝返回）',
    `callback_status` TINYINT      NOT NULL DEFAULT 0 COMMENT '回调处理状态：0=未收到回调 1=已处理 2=处理失败（用于幂等控制）',
    `callback_data`   JSON         DEFAULT NULL COMMENT '第三方回调原始数据（微信/支付宝回调报文）',
    `pay_time`        BIGINT       DEFAULT NULL COMMENT '支付成功时间（毫秒时间戳）',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_status` (`user_id`, `pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- ============================================================
-- 千语商城 - 支付 - 退款记录表
-- ============================================================
-- 业务说明: 退款记录表 —— 记录每笔退款的流水，关联原支付单和售后单。
--           refund_channel 区分退款去向（原路退回/退到余额），transaction_id 记录第三方退款流水号用于对账，
--           callback_data 记录第三方退款回调原始数据。
-- 分片策略: 按 order_id 分 16 表
-- 分片键  : order_id
-- 预估量级: 日均 5 万笔，保留 1 年 约 1800 万行
-- ============================================================



CREATE TABLE IF NOT EXISTS `pay_refund` (
    `id`             BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `refund_no`      VARCHAR(32)  NOT NULL COMMENT '退款单号',
    `payment_id`     BIGINT       NOT NULL COMMENT '关联支付记录 ID',
    `order_id`       BIGINT       NOT NULL COMMENT '关联订单 ID',
    `after_sale_id`  BIGINT       DEFAULT NULL COMMENT '关联售后单 ID',
    `amount`         DECIMAL(20,6) NOT NULL COMMENT '退款金额（元）',
    `reason`         VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
    `refund_channel` TINYINT      NOT NULL DEFAULT 1 COMMENT '退款渠道：1=原路退回 2=退到余额',
    `refund_status`  TINYINT      NOT NULL DEFAULT 10 COMMENT '退款状态：10=待退款 20=退款成功 30=退款失败',
    `transaction_id` VARCHAR(64)  DEFAULT NULL COMMENT '第三方退款流水号（微信/支付宝返回）',
    `callback_data`  JSON         DEFAULT NULL COMMENT '第三方退款回调原始数据',
    `refund_time`    BIGINT       DEFAULT NULL COMMENT '退款完成时间（毫秒时间戳）',
    `create_time`    BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`    BIGINT       NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_payment_id` (`payment_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- ------------------------------------------------------------
-- INV 库存  (2 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 库存 - SKU 库存表
-- ============================================================
-- 业务说明: SKU 库存表 —— 记录每个 SKU 的可用库存、锁定库存和安全库存，
--           是库存扣减和预占的核心数据表。使用 version 乐观锁防止并发超卖。
-- 分片策略: 按 sku_id 分 8 表
-- 分片键  : sku_id
-- 预估量级: SKU 数 约 100 万行
-- ============================================================



CREATE TABLE IF NOT EXISTS `inv_stock` (
    `id`               BIGINT  NOT NULL COMMENT '主键（Snowflake ID）',
    `sku_id`           BIGINT  NOT NULL COMMENT 'SKU ID',
    `available_stock`  INT     NOT NULL DEFAULT 0 COMMENT '可用库存',
    `locked_stock`     INT     NOT NULL DEFAULT 0 COMMENT '锁定库存（已下单未发货）, 出库扣减(商家已发货会减少)',
    `safety_stock`     INT     NOT NULL DEFAULT 0 COMMENT '安全库存（预警阈值）',
    `version`          BIGINT  NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，每次更新 +1',
    `create_time`      BIGINT  NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`      BIGINT  NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`          TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU 库存表';

-- ============================================================
-- 千语商城 - 库存 - 库存变更日志表
-- ============================================================
-- 业务说明: 库存变更日志表 —— 记录每次库存变动的明细，包括变动前后库存量、变动类型及关联订单，
--           用于库存对账和审计追溯。归档策略: 保留最近 90 天在线数据，超期数据迁移至 inv_stock_log_archive 冷表。
-- 分片策略: 按 sku_id 分 8 表
-- 分片键  : sku_id
-- 预估量级: 日均 200 万条，保留半年 约 3.6 亿行
-- ============================================================



CREATE TABLE IF NOT EXISTS `inv_stock_log` (
    `id`           BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `sku_id`       BIGINT       NOT NULL COMMENT 'SKU ID',
    `order_id`     BIGINT       DEFAULT NULL COMMENT '关联订单 ID',
    `type`         TINYINT      NOT NULL COMMENT '变更类型：1=商家调整 2=下单锁定 3=出库解锁 4=退单回滚',
    `quantity`     INT          NOT NULL COMMENT '变更数量（正数增加，负数减少）',
    `before_stock` INT          NOT NULL COMMENT '变更前可用库存',
    `after_stock`  INT          NOT NULL COMMENT '变更后可用库存',
    `remark`       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `archived`     TINYINT      NOT NULL DEFAULT 0 COMMENT '归档标记: 0=在线 1=已归档',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变更日志表';

-- ------------------------------------------------------------
-- SMS 营销  (5 张)
-- ------------------------------------------------------------

-- 来源: 千语商城 -- 数据库表结构汇总.md
CREATE TABLE IF NOT EXISTS `sms_coupon` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`     BIGINT        NOT NULL DEFAULT 0 COMMENT '商家ID（0=平台券，>0=商家券）',
    `name`            VARCHAR(64)   NOT NULL COMMENT '优惠券名称',
    `type`            TINYINT       NOT NULL COMMENT '优惠券类型：1=满减 2=折扣 3=免运费',
    `scope_type`      TINYINT       NOT NULL DEFAULT 1 COMMENT '适用范围：1=全平台 2=指定商家 3=指定品类 4=指定商品',
    `scope_value`     JSON          DEFAULT NULL COMMENT '适用范围值：scope_type=2时为商家ID数组，=3时为分类ID数组，=4时为SPU ID数组',
    `threshold`       DECIMAL(20,6) NOT NULL COMMENT '使用门槛金额（元）',
    `discount_amount` DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（元），满减券使用',
    `discount_rate`   DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '折扣率（如 8.50 表示 85 折），折扣券使用',
    `total_count`     INT           NOT NULL DEFAULT 0 COMMENT '发放总量',
    `remain_count`    INT           NOT NULL DEFAULT 0 COMMENT '剩余库存',
    `per_limit`       INT           NOT NULL DEFAULT 1 COMMENT '每人限领数量',
    `start_time`      BIGINT        NOT NULL COMMENT '生效时间（毫秒时间戳）',
    `end_time`        BIGINT        NOT NULL COMMENT '失效时间（毫秒时间戳）',
    `status`          TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0=禁用 1=启用 2=已过期',
    `create_time`     BIGINT        NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT        NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_time` (`status`, `start_time`, `end_time`),
    KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券定义表';

-- 来源: 千语商城 -- 数据库表结构汇总.md
CREATE TABLE IF NOT EXISTS `sms_flash_sale` (
    `id`            BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `promotion_id`  BIGINT        NOT NULL COMMENT '关联促销活动 ID',
    `sku_id`        BIGINT        NOT NULL COMMENT '参与秒杀的 SKU ID',
    `flash_price`   DECIMAL(20,6) NOT NULL COMMENT '秒杀价格（元）',
    `total_stock`   INT           NOT NULL COMMENT '秒杀总库存',
    `remain_stock`  INT           NOT NULL COMMENT '剩余库存',
    `per_limit`     INT           NOT NULL DEFAULT 1 COMMENT '每人限购数量',
    `start_time`    BIGINT        NOT NULL COMMENT '秒杀开始时间（毫秒时间戳）',
    `end_time`      BIGINT        NOT NULL COMMENT '秒杀结束时间（毫秒时间戳）',
    `status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0=禁用 1=启用 2=已结束',
    `create_time`   BIGINT        NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`   BIGINT        NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_promotion_id` (`promotion_id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 来源: 千语商城 -- 数据库表结构汇总.md
CREATE TABLE IF NOT EXISTS `sms_promotion` (
    `id`           BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`  BIGINT       NOT NULL COMMENT '商家 ID',
    `name`         VARCHAR(128) NOT NULL COMMENT '活动名称',
    `type`         TINYINT      NOT NULL COMMENT '活动类型：1=打折 2=满赠 3=秒杀',
    `rules`        JSON         DEFAULT NULL COMMENT '活动规则（JSON 格式，如折扣比例、赠品条件等）',
    `start_time`   BIGINT       NOT NULL COMMENT '活动开始时间（毫秒时间戳）',
    `end_time`     BIGINT       NOT NULL COMMENT '活动结束时间（毫秒时间戳）',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=禁用 1=启用 2=已结束',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`  BIGINT       NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动表';

-- 来源: 千语商城 -- 数据库表结构汇总.md
CREATE TABLE IF NOT EXISTS `sms_user_coupon` (
    `id`           BIGINT  NOT NULL COMMENT '主键（Snowflake ID）',
    `coupon_id`    BIGINT  NOT NULL COMMENT '优惠券定义 ID',
    `source_type`  TINYINT NOT NULL DEFAULT 1 COMMENT '领取来源: 1=主动领取 2=系统发放 3=活动派发 4=邀请奖励',
    `user_id`      BIGINT  NOT NULL COMMENT '领取用户 ID',
    `order_id`     BIGINT  DEFAULT NULL COMMENT '核销订单 ID',
    `status`       TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=未使用 2=已使用 3=已过期',
    `use_time`     BIGINT  DEFAULT NULL COMMENT '使用时间（毫秒时间戳）',
    `expire_time`  BIGINT  NOT NULL COMMENT '过期时间（毫秒时间戳）',
    `create_time`  BIGINT  NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`  BIGINT  NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`      TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 来源: 千语商城 -- 数据库表结构汇总.md
CREATE TABLE IF NOT EXISTS `sms_user_coupon_log` (
    `id`              BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `user_coupon_id`  BIGINT       NOT NULL COMMENT '关联 sms_user_coupon.id',
    `user_id`         BIGINT       NOT NULL COMMENT '用户 ID',
    `coupon_id`       BIGINT       NOT NULL COMMENT '优惠券模板 ID',
    `action`          TINYINT      NOT NULL COMMENT '操作类型: 1=领取(CLAIM) 2=核销(USE) 3=回滚(ROLLBACK) 4=过期(EXPIRE)',
    `order_id`        BIGINT       DEFAULT NULL COMMENT '关联订单 ID（USE/ROLLBACK 时有值）',
    `order_no`        VARCHAR(32)  DEFAULT NULL COMMENT '订单编号（冗余，方便查询）',
    `remark`          VARCHAR(256) NOT NULL DEFAULT '' COMMENT '备注（如"订单取消回滚"/"售后退款回滚"）',
    `create_time`     BIGINT       NOT NULL COMMENT '操作时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_user_coupon_id` (`user_coupon_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券使用记录表';

-- ------------------------------------------------------------
-- LOG 物流  (2 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 物流 - 物流轨迹表
-- ============================================================
-- 业务说明: 记录物流运单的实时轨迹信息，由物流回调或主动查询写入。
--           source 记录查询来源（回调/主动查询/用户触发），carrier_code 记录实际返回数据的物流公司，
--           raw_data 保留第三方原始报文用于对账排错。
-- 分片策略: 按 shipping_id 分 16 表
-- 分片键  : shipping_id
-- 预估量级: 每个运单 5~20 条轨迹，日均 10~100 万
-- ============================================================



CREATE TABLE IF NOT EXISTS `log_delivery_trace` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `shipping_id`     BIGINT        NOT NULL COMMENT '物流单ID（关联 log_shipping.id）',
    `trace_time`      BIGINT        NOT NULL COMMENT '轨迹发生时间（毫秒时间戳）',
    `description`     VARCHAR(512)  NOT NULL COMMENT '轨迹描述，如 "快件已从北京转运中心发出"',
    `location`        VARCHAR(128)  DEFAULT NULL COMMENT '所在城市/地区，如 "北京市"',
    `source`          TINYINT       NOT NULL DEFAULT 1 COMMENT '查询来源: 1=第三方回调推送 2=主动查询第三方 3=用户手动触发查询',
    `carrier_code`    VARCHAR(32)   DEFAULT NULL COMMENT '物流公司编码（如 SF=顺丰, YTO=圆通），记录实际返回数据的物流商',
    `raw_data`        JSON          DEFAULT NULL COMMENT '第三方返回的原始数据，用于对账和排错',
    `create_time`     BIGINT        NOT NULL COMMENT '记录创建时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_shipping_id` (`shipping_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';

-- ============================================================
-- 千语商城 - 物流 - 物流发货单表
-- ============================================================
-- 业务说明: 记录订单的物流发货信息，一个订单可拆多个物流单
-- 分片策略: 按 order_id 分 16 表
-- 分片键  : order_id
-- 预估量级: 与订单量持平，日均 1~5 万
-- ============================================================



CREATE TABLE IF NOT EXISTS `log_shipping` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `order_id`        BIGINT        NOT NULL COMMENT '订单ID',
    `order_item_id`   BIGINT        DEFAULT NULL COMMENT '订单明细ID（拆物流时指定具体商品）',
    `shipping_no`     VARCHAR(64)   NOT NULL COMMENT '物流运单号',
    `shipping_company` VARCHAR(64)  NOT NULL COMMENT '物流公司编码（如 SF=顺丰、YTO=圆通）',
    `shipping_company_name` VARCHAR(64) DEFAULT NULL COMMENT '物流公司名称',
    `status`          TINYINT       NOT NULL DEFAULT 0 COMMENT '物流状态: 0=已发货 1=运输中 2=已签收 3=异常',
    `delivery_time`   BIGINT        DEFAULT NULL COMMENT '发货时间（毫秒时间戳）',
    `receive_time`    BIGINT        DEFAULT NULL COMMENT '签收时间（毫秒时间戳）',
    `create_time`     BIGINT        NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT        NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_shipping_no` (`shipping_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流发货单表';

-- ------------------------------------------------------------
-- MCH 商家  (10 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 商家 - 商家账户表
-- ============================================================
-- 业务说明: 商家资金账户表，记录商家可用余额、冻结金额、累计收支。
--           每个商家一条记录，下单完成时入账、退款时扣减、提现时冻结。
--           所有金额变动通过 mch_bill 流水记录，账户余额由流水汇总得出。
-- 分片策略: 不分片（每个商家一条记录，全局查询）
-- 分片键  : 无
-- 预估量级: 与商家数一致（万级）
-- ============================================================



CREATE TABLE IF NOT EXISTS `mch_account` (
    `id`               BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`      BIGINT       NOT NULL COMMENT '商家ID（唯一，每个商家一条记录）',
    `balance`          DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '可用余额（元），可提现',
    `frozen_amount`    DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '冻结金额（元），提现审核中/退款待处理',
    `total_income`     DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '累计总收入（元）',
    `total_withdraw`   DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '累计已提现（元）',
    `total_refund`     DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '累计退款支出（元）',
    `total_commission` DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '累计平台佣金（元）',
    `version`          BIGINT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，每次更新 +1',
    `create_time`      BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`      BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家账户表';

-- ============================================================
-- 千语商城 - 商家 - 商家账单明细表
-- ============================================================
-- 业务说明: 商家账单流水表，记录每笔资金变动（订单收入、退款支出、佣金调整）。
--           订单完成时生成收入账单，退款完成时生成支出账单。
--           每条账单明细记录订单金额、平台佣金、主播佣金、商家实得的完整拆分。
-- 分片策略: 按 merchant_id 分 16 表
-- 分片键  : merchant_id
-- 预估量级: 日均 50 万笔（与订单量相当）
-- ============================================================



CREATE TABLE IF NOT EXISTS `mch_bill` (
    `id`               BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`      BIGINT       NOT NULL COMMENT '商家ID',
    `order_id`         BIGINT       NOT NULL COMMENT '关联订单ID',
    `order_no`         VARCHAR(32)  NOT NULL COMMENT '订单编号（冗余，方便查询）',
    `type`             TINYINT      NOT NULL COMMENT '账单类型: 1=订单收入 2=退款支出 3=佣金调整',
    `order_amount`     DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '订单金额（元）',
    `refund_amount`    DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '退款金额（元，退款账单时有值）',
    `platform_fee`     DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '平台佣金（元）',
    `platform_rate`    DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '平台佣金比例（%，如 5.00 表示 5%）',
    `anchor_fee`       DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '主播佣金（元，非直播为0）',
    `anchor_rate`      DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '主播佣金比例（%，非直播为0）',
    `merchant_income`  DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '商家实际入账金额（元），= order_amount - platform_fee - anchor_fee - refund_amount',
    `settlement_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '关联结算单ID（结算后写入，0=未结算）',
    `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '结算状态: 0=未结算 1=已结算',
    `remark`           VARCHAR(256) NOT NULL DEFAULT '' COMMENT '备注',
    `create_time`      BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_merchant_status` (`merchant_id`, `status`),
    KEY `idx_settlement_id` (`settlement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家账单明细表';

-- ============================================================
-- 千语商城 - 商家 - 运费规则表
-- ============================================================
-- 业务说明: 运费规则表，定义运费模板下的具体区域规则。每个模板必须有一条默认规则
--          （destination_type=1，覆盖全国），可叠加指定地区规则（如新疆/西藏加价）。
--          下单时先匹配指定地区规则，未匹配到则使用默认规则。
-- 分片策略: 不分片（规则数量可控）
-- 分片键  : 无
-- 预估量级: 每模板 2~10 条规则，十万级
-- ============================================================



CREATE TABLE IF NOT EXISTS `mch_freight_rule` (
    `id`                BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `template_id`       BIGINT       NOT NULL COMMENT '关联运费模板ID',
    `destination_type`  TINYINT      NOT NULL COMMENT '目标地区类型: 1=默认(全国) 2=指定地区',
    `destination`       JSON         DEFAULT NULL COMMENT '指定地区列表(省份), 格式: ["新疆","西藏","青海"]（type=1时为NULL）',
    `first_unit`        DECIMAL(10,2) NOT NULL COMMENT '首件/首kg/首m³ 数量',
    `first_price`       DECIMAL(20,6) NOT NULL COMMENT '首件价格（元）',
    `additional_unit`   DECIMAL(10,2) NOT NULL COMMENT '续件/续kg/续m³ 数量',
    `additional_price`  DECIMAL(20,6) NOT NULL COMMENT '续件价格（元）',
    `create_time`       BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`       BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运费规则表';

-- ============================================================
-- 千语商城 - 商家 - 运费模板表
-- ============================================================
-- 业务说明: 运费模板表，商家创建运费计价规则模板。支持三种计价方式（按件数/重量/体积），
--          三种包邮条件（不包邮/满金额/满件数）。商品（pms_spu）通过 freight_template_id
--          关联模板，下单时根据收货地址匹配模板下的具体区域规则（mch_freight_rule）计算运费。
-- 分片策略: 不分片（数据量小，按 merchant_id 查询）
-- 分片键  : 无
-- 预估量级: 每商家 1~10 个模板，万级
-- ============================================================



CREATE TABLE IF NOT EXISTS `mch_freight_template` (
    `id`                  BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`         BIGINT       NOT NULL COMMENT '商家ID',
    `name`                VARCHAR(64)  NOT NULL COMMENT '模板名称（如"标准快递"、"重货物流"）',
    `billing_type`        TINYINT      NOT NULL COMMENT '计价方式: 1=按件数 2=按重量(kg) 3=按体积(m³)',
    `free_shipping_type`  TINYINT      NOT NULL DEFAULT 0 COMMENT '包邮条件: 0=不包邮 1=满金额包邮 2=满件数包邮',
    `free_shipping_value` DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '包邮门槛值（金额或件数，0=无条件包邮）',
    `is_default`          TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认模板: 0=否 1=是（每个商家只有一个默认模板）',
    `status`              TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    `create_time`         BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`         BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`             TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运费模板表';

-- ============================================================
-- 千语商城 - 商家 - 商家表
-- ============================================================
-- 业务说明: 记录入驻商家信息，包括个人/企业资质审核
-- 分片策略: 不分片（商家数量有限，全局查询）
-- 分片键  : N/A
-- 预估量级: 数千~数万商家
-- ============================================================



CREATE TABLE IF NOT EXISTS `mch_merchant` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `user_id`         BIGINT        NOT NULL COMMENT '关联用户ID（user_info.user_id）',
    `name`            VARCHAR(128)  NOT NULL COMMENT '商家名称',
    `type`            TINYINT       NOT NULL DEFAULT 1 COMMENT '商家类型: 1=个人 2=企业',
    `contact_name`    VARCHAR(64)   NOT NULL COMMENT '联系人姓名',
    `contact_phone`   VARCHAR(20)   NOT NULL COMMENT '联系电话',
    `license_no`      VARCHAR(64)   DEFAULT NULL COMMENT '营业执照编号（企业商家必填）',
    `license_image`   VARCHAR(512)  DEFAULT NULL COMMENT '营业执照图片URL',
    `description`     VARCHAR(1000) DEFAULT NULL COMMENT '商家简介',
    `bank_name`       VARCHAR(128)  DEFAULT NULL COMMENT '结算银行名称',
    `bank_account`    VARCHAR(64)   DEFAULT NULL COMMENT '结算银行账号（加密存储）',
    `bank_holder`     VARCHAR(64)   DEFAULT NULL COMMENT '开户人姓名（加密存储）',
    `settlement_cycle` TINYINT      NOT NULL DEFAULT 1 COMMENT '结算周期: 1=T+1 2=T+7 3=T+15',
    `audit_status`    TINYINT       NOT NULL DEFAULT 0 COMMENT '审核状态: 0=待审核 1=已通过 2=已拒绝',
    `audit_remark`    VARCHAR(255)  DEFAULT NULL COMMENT '审核备注/拒绝原因',
    `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '商家状态: 0=禁用 1=正常 2=冻结',
    `create_time`     BIGINT        NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT        NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- ============================================================
-- Module: MCH 商家管理
-- Table: mch_merchant_cert 商户认证表
-- Desc:   记录商户实名认证和企业资质认证信息，一个商户可有多次认证记录
-- Sharding: 不分片（与 mch_merchant 同库）
-- Volume:  与商家数一致（万级）
-- ============================================================



CREATE TABLE IF NOT EXISTS `mch_merchant_cert` (
    `id`               BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`      BIGINT       NOT NULL COMMENT '商家ID',
    `cert_type`        TINYINT      NOT NULL COMMENT '认证类型: 1=个人实名 2=企业认证',
    `cert_name`        VARCHAR(128) NOT NULL COMMENT '认证人/企业名称',
    `cert_no`          VARCHAR(64)  NOT NULL COMMENT '证件号码（身份证号/营业执照号，加密存储）',
    `cert_front_image` VARCHAR(512) DEFAULT NULL COMMENT '证件正面照片URL',
    `cert_back_image`  VARCHAR(512) DEFAULT NULL COMMENT '证件反面照片URL（个人身份证需要）',
    `cert_holder_image` VARCHAR(512) DEFAULT NULL COMMENT '手持证件照片URL（个人认证可选）',
    `legal_person`     VARCHAR(64)  DEFAULT NULL COMMENT '法人姓名（企业认证）',
    `legal_person_id`  VARCHAR(64)  DEFAULT NULL COMMENT '法人身份证号（企业认证，加密存储）',
    `address`          VARCHAR(512) DEFAULT NULL COMMENT '企业地址（企业认证）',
    `audit_status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '审核状态: 0=待审核 1=已通过 2=已拒绝',
    `audit_remark`     VARCHAR(255) DEFAULT NULL COMMENT '审核备注/拒绝原因',
    `audit_time`       BIGINT       NOT NULL DEFAULT 0 COMMENT '审核时间（毫秒时间戳）',
    `create_time`      BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`      BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_type` (`merchant_id`, `cert_type`, `deleted`),
    KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户认证表';

-- 来源: 千语商城 -- 数据库表结构汇总.md
CREATE TABLE IF NOT EXISTS `mch_settlement` (
    `id`                BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `settlement_no`     VARCHAR(32)  NOT NULL COMMENT '结算单号（唯一，如 ST20260527000001）',
    `merchant_id`       BIGINT       NOT NULL COMMENT '商家ID',
    `start_time`        BIGINT       NOT NULL COMMENT '结算周期开始时间（毫秒时间戳）',
    `end_time`          BIGINT       NOT NULL COMMENT '结算周期结束时间（毫秒时间戳）',
    `order_count`       INT          NOT NULL DEFAULT 0 COMMENT '本期订单笔数',
    `order_amount`      DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '本期订单总金额（元）',
    `refund_count`      INT          NOT NULL DEFAULT 0 COMMENT '本期退款笔数',
    `refund_amount`     DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '本期退款总金额（元）',
    `platform_fee`      DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '本期平台佣金（元）',
    `anchor_fee`        DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '本期主播佣金（元）',
    `settlement_amount` DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '本期应结金额（元），= order_amount - refund_amount - platform_fee - anchor_fee',
    `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '结算状态: 0=待结算 1=已结算 2=已打款',
    `settle_time`       BIGINT       NOT NULL DEFAULT 0 COMMENT '结算时间（毫秒时间戳）',
    `create_time`       BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`       BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_settlement_no` (`settlement_no`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_merchant_status` (`merchant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家结算单表';

-- ============================================================
-- Module: MCH 商家管理
-- Table: mch_settlement_info 商户结算信息表
-- Desc:   记录商户的收款账户/结算方式，支持银行卡/支付宝/微信等多种渠道
-- Sharding: 不分片（与 mch_merchant 同库）
-- Volume:  每商家 1~3 条（万级）
-- ============================================================



CREATE TABLE IF NOT EXISTS `mch_settlement_info` (
    `id`              BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`     BIGINT       NOT NULL COMMENT '商家ID',
    `settlement_type` TINYINT      NOT NULL DEFAULT 1 COMMENT '结算方式: 1=银行卡 2=支付宝 3=微信',
    `account_name`    VARCHAR(64)  NOT NULL COMMENT '账户持有人姓名',
    `account_no`      VARCHAR(128) NOT NULL COMMENT '账户号码（银行卡号/支付宝账号，加密存储）',
    `bank_name`       VARCHAR(128) DEFAULT NULL COMMENT '开户银行名称（银行卡时有值）',
    `bank_branch`     VARCHAR(256) DEFAULT NULL COMMENT '开户支行名称',
    `bank_code`       VARCHAR(32)  DEFAULT NULL COMMENT '银行编码（联行号）',
    `is_default`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认: 0=否 1=是',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用 1=正常',
    `create_time`     BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户结算信息表';

-- ============================================================
-- 千语商城 - 商家 - 店铺表
-- ============================================================
-- 业务说明: 商家旗下的店铺信息，一个商家可拥有多个店铺
-- 分片策略: 不分片（店铺数量有限，全局查询）
-- 分片键  : N/A
-- 预估量级: 数千~数万店铺
-- ============================================================



CREATE TABLE IF NOT EXISTS `mch_store` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `merchant_id`     BIGINT        NOT NULL COMMENT '所属商家ID（关联 mch_merchant.id）',
    `name`            VARCHAR(128)  NOT NULL COMMENT '店铺名称',
    `contact_phone`   VARCHAR(20)   DEFAULT NULL COMMENT '店铺联系电话',
    `logo`            VARCHAR(512)  DEFAULT NULL COMMENT '店铺Logo URL',
    `cover_image`     VARCHAR(512)  DEFAULT NULL COMMENT '店铺封面图 URL',
    `description`     VARCHAR(1000) DEFAULT NULL COMMENT '店铺简介',
    `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '店铺状态: 0=关闭 1=正常 2=装修中',
    `create_time`     BIGINT        NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT        NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- ============================================================
-- 千语商城 - 商家 - 商家提现记录表
-- ============================================================
-- 业务说明: 商家提现记录表，记录商家从账户余额提现到银行卡/支付宝的每笔申请。
--           商家发起提现 → 冻结余额 → 平台审核 → 打款 → 解冻扣减。
--           审核拒绝时解冻退回余额。
-- 分片策略: 按 merchant_id 分 16 表（与 mch_account 同分片）
-- 分片键  : merchant_id
-- 预估量级: 每商家每月 1~4 次，万级
-- ============================================================



CREATE TABLE IF NOT EXISTS `mch_withdrawal` (
    `id`               BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `withdrawal_no`    VARCHAR(32)  NOT NULL COMMENT '提现单号（唯一，如 WD20260527000001）',
    `merchant_id`      BIGINT       NOT NULL COMMENT '商家ID',
    `amount`           DECIMAL(20,6) NOT NULL COMMENT '提现金额（元）',
    `bank_name`        VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '收款银行名称',
    `bank_account`     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '收款银行账号（脱敏存储）',
    `account_name`     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '收款人姓名',
    `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '提现状态: 0=待审核 1=审核通过 2=打款中 3=打款成功 4=审核拒绝 5=打款失败',
    `reject_reason`    VARCHAR(256) NOT NULL DEFAULT '' COMMENT '拒绝原因',
    `transfer_no`      VARCHAR(64)  DEFAULT NULL COMMENT '打款流水号（银行/第三方返回）',
    `transfer_time`    BIGINT       NOT NULL DEFAULT 0 COMMENT '打款成功时间（毫秒时间戳）',
    `create_time`      BIGINT       NOT NULL COMMENT '申请时间（毫秒时间戳）',
    `update_time`      BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_withdrawal_no` (`withdrawal_no`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_merchant_status` (`merchant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家提现记录表';

-- ------------------------------------------------------------
-- REV 评价  (2 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 评价 - 商品评价表
-- ============================================================
-- 业务说明: 用户对已完成订单商品的评价，支持文字+图片+匿名
-- 分片策略: 按 spu_id 分 8 表（按商品维度查询评价是最高频操作）
-- 分片键  : spu_id
-- 预估量级: 日均 1~10 万条
-- ============================================================



CREATE TABLE IF NOT EXISTS `rev_review` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `order_id`        BIGINT        NOT NULL COMMENT '订单ID',
    `order_item_id`   BIGINT        NOT NULL COMMENT '订单明细ID',
    `user_id`         BIGINT        NOT NULL COMMENT '评价用户ID',
    `spu_id`          BIGINT        NOT NULL COMMENT '商品SPU ID',
    `sku_id`          BIGINT        NOT NULL COMMENT '商品SKU ID',
    `sku_name`        VARCHAR(256)  DEFAULT NULL COMMENT 'SKU名称快照（评价时锁定的规格名称）',
    `merchant_id`     BIGINT        NOT NULL COMMENT '商家ID',
    `score`           TINYINT       NOT NULL DEFAULT 5 COMMENT '评分: 1~5 分',
    `content`         VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
    `images`          JSON          DEFAULT NULL COMMENT '评价图片列表, 格式: ["url1","url2"]',
    `is_anonymous`    TINYINT       NOT NULL DEFAULT 0 COMMENT '是否匿名: 0=否 1=是',
    `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '评价状态: 0=隐藏 1=正常 2=违规',
    `reply_content`   VARCHAR(500)  DEFAULT NULL COMMENT '商家回复内容',
    `reply_time`      BIGINT        DEFAULT NULL COMMENT '商家回复时间（毫秒时间戳）',
    `create_time`     BIGINT        NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT        NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_order_item` (`order_id`, `order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- ============================================================
-- 千语商城 - 评价 - 评价统计表
-- ============================================================
-- 业务说明: 评价统计表 —— 预聚合评价数据（好评/中评/差评/带图/好评率），避免前端展示时实时 COUNT。
--           spu_id + sku_id 组合唯一，sku_id=0 表示 SPU 级汇总，非零表示具体 SKU 级统计。
--           评价写入/删除时异步更新此表，pms_spu 的 comment_count、avg_score 从此表 SPU 汇总行同步。
-- 分片策略: 不分片（数据量可控，按 spu_id 查询）
-- 分片键  : 无
-- 预估量级: SPU 数 × 平均 SKU 数 ≈ 数百万行
-- ============================================================



CREATE TABLE IF NOT EXISTS `rev_review_stat` (
    `id`              BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `spu_id`          BIGINT       NOT NULL COMMENT 'SPU ID',
    `sku_id`          BIGINT       NOT NULL DEFAULT 0 COMMENT 'SKU ID（0=SPU 汇总，非零=具体 SKU 统计）',
    `total_count`     INT          NOT NULL DEFAULT 0 COMMENT '总评价数',
    `good_count`      INT          NOT NULL DEFAULT 0 COMMENT '好评数（score >= 4）',
    `mid_count`       INT          NOT NULL DEFAULT 0 COMMENT '中评数（score = 3）',
    `bad_count`       INT          NOT NULL DEFAULT 0 COMMENT '差评数（score <= 2）',
    `image_count`     INT          NOT NULL DEFAULT 0 COMMENT '带图评价数',
    `avg_score`       DECIMAL(2,1) NOT NULL DEFAULT 0.0 COMMENT '平均评分（1.0~5.0）',
    `good_rate`       DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '好评率（百分比，如 98.50 表示 98.50%）',
    `update_time`     BIGINT       NOT NULL DEFAULT 0 COMMENT '最后更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_sku` (`spu_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价统计表';

-- ------------------------------------------------------------
-- ADS 地址  (2 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 地址 - 用户收货地址表
-- ============================================================
-- 业务说明: 用户管理收货地址，支持设置默认地址和地址标签（家/公司/学校）
-- 分片策略: 按 user_id 分 8 表
-- 分片键  : user_id
-- 预估量级: 人均 2~3 条，总量约百万级
-- ============================================================



CREATE TABLE IF NOT EXISTS `ads_address` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `user_id`         BIGINT        NOT NULL COMMENT '用户ID',
    `name`            VARCHAR(64)   NOT NULL COMMENT '收货人姓名',
    `phone`           VARCHAR(20)   NOT NULL COMMENT '收货人手机号',
    `country`         VARCHAR(3)    NOT NULL DEFAULT 'CN' COMMENT '2位标准国家代码',
    `province`        VARCHAR(32)   NOT NULL COMMENT '省',
    `city`            VARCHAR(32)   NOT NULL COMMENT '市',
    `district`        VARCHAR(32)   NOT NULL COMMENT '区/县',
    `detail`          VARCHAR(256)  NOT NULL COMMENT '详细地址（街道、门牌号等）',
    `province_code`   VARCHAR(12)   DEFAULT NULL COMMENT '省编码（关联 ads_region.code）',
    `city_code`       VARCHAR(12)   DEFAULT NULL COMMENT '市编码',
    `district_code`   VARCHAR(12)   DEFAULT NULL COMMENT '区编码',
    `is_default`      TINYINT       NOT NULL DEFAULT 0 COMMENT '是否默认: 0=否 1=是',
    `tag`             VARCHAR(32)   DEFAULT NULL COMMENT '地址标签：家/公司/学校等',
    `create_time`     BIGINT        NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT        NOT NULL COMMENT '更新时间（毫秒时间戳）',
    `deleted`         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- ============================================================
-- 千语商城 - 地址 - 行政区划表
-- ============================================================
-- 业务说明: 全国省/市/区三级行政区划数据，通过 parent_id 实现树形结构
-- 分片策略: 不分片（固定数据，约 3000 条）
-- 分片键  : N/A
-- 预估量级: 约 3000 条（全国省市区）
-- ============================================================



CREATE TABLE IF NOT EXISTS `ads_region` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID，非物理主键）',
    `parent_id`       BIGINT        NOT NULL DEFAULT 0 COMMENT '父级ID（0=顶级/省）',
    `name`            VARCHAR(64)   NOT NULL COMMENT '区域名称',
    `level`           TINYINT       NOT NULL COMMENT '层级: 1=省 2=市 3=区/县',
    `code`            VARCHAR(12)   NOT NULL COMMENT '行政区划代码（国家统计局标准）',
    `sort`            INT           NOT NULL DEFAULT 0 COMMENT '排序值',
    `create_time`     BIGINT        NOT NULL COMMENT '创建时间（毫秒时间戳）',
    PRIMARY KEY (`code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行政区划表';

-- ------------------------------------------------------------
-- FAV 收藏  (1 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 收藏 - 收藏表
-- ============================================================
-- 业务说明: 用户收藏商品或店铺，通过 target_type 区分
-- 分片策略: 按 user_id 分 8 表
-- 分片键  : user_id
-- 预估量级: 人均 10~50 条，总量约千万级
-- ============================================================



CREATE TABLE IF NOT EXISTS `fav_favorite` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `user_id`         BIGINT        NOT NULL COMMENT '用户ID',
    `target_id`       BIGINT        NOT NULL COMMENT '目标ID（SPU ID 或店铺 ID）',
    `target_type`     TINYINT       NOT NULL COMMENT '目标类型: 1=商品(SPU) 2=店铺',
    `create_time`     BIGINT        NOT NULL COMMENT '收藏时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
    KEY `idx_target` (`target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ------------------------------------------------------------
-- HIS 历史  (2 张)
-- ------------------------------------------------------------

-- ============================================================
-- 千语商城 - 历史 - 浏览记录表
-- ============================================================
-- 业务说明: 记录用户浏览过的商品（SPU），用于"浏览历史"列表和"猜你喜欢"推荐。
--           同一用户同一商品多次浏览只更新时间，不重复插入。
--           商品名称/图片/价格做快照，定期清理 90 天前数据。
-- 分片策略: 按 user_id 分 8 表
-- 分片键  : user_id
-- 预估量级: 人均 100~500 条，1000 万用户 约 10~50 亿行（需定期清理）
-- ============================================================



CREATE TABLE IF NOT EXISTS `his_browse_history` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `user_id`         BIGINT        NOT NULL COMMENT '用户ID',
    `spu_id`          BIGINT        NOT NULL COMMENT '商品SPU ID',
    `spu_name`        VARCHAR(256)  NOT NULL DEFAULT '' COMMENT '商品名称快照',
    `spu_image`       VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '商品主图URL快照',
    `price`           DECIMAL(20,6) NOT NULL DEFAULT 0.00 COMMENT '浏览时的最低SKU价格（元）',
    `browse_time`     BIGINT        NOT NULL COMMENT '浏览时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_spu` (`user_id`, `spu_id`),
    KEY `idx_user_time` (`user_id`, `browse_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览记录表';

-- ============================================================
-- 千语商城 - 历史 - 搜索热词表
-- ============================================================
-- 业务说明: 记录用户搜索关键词及热度，用于搜索首页"热门搜索"展示。
--           定时任务每小时聚合搜索次数，更新 heat 排序。
-- 分片策略: 不分片（数据量可控，全局查询）
-- 分片键  : N/A
-- 预估量级: 万级（热词去重后约 5000~20000 条）
-- ============================================================



CREATE TABLE IF NOT EXISTS `his_search_keyword` (
    `id`              BIGINT        NOT NULL COMMENT '主键（Snowflake ID）',
    `keyword`         VARCHAR(128)  NOT NULL COMMENT '搜索关键词',
    `heat`            INT           NOT NULL DEFAULT 0 COMMENT '热度（搜索次数）',
    `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0=隐藏 1=正常',
    `create_time`     BIGINT        NOT NULL COMMENT '首次搜索时间（毫秒时间戳）',
    `update_time`     BIGINT        NOT NULL COMMENT '最近更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword` (`keyword`),
    KEY `idx_heat` (`heat` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索热词表';

-- ------------------------------------------------------------
-- CMS 内容管理  (3 张)
-- ------------------------------------------------------------

-- 来源: 千语商城 -- 数据库表结构汇总.md
CREATE TABLE IF NOT EXISTS `cms_banner` (
    `id`           BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `title`        VARCHAR(128) NOT NULL COMMENT 'Banner 标题',
    `description`  VARCHAR(256) DEFAULT NULL COMMENT '描述文案',
    `action_text`  VARCHAR(64)  DEFAULT NULL COMMENT '按钮文案（如"立即抢购"）',
    `tag_text`     VARCHAR(64)  DEFAULT NULL COMMENT '标签文案（如"今日焦点"）',
    `image`        VARCHAR(512) NOT NULL COMMENT '图片 URL',
    `link_url`     VARCHAR(512) DEFAULT NULL COMMENT '跳转 URL（兼容字段）',
    `link_type`    TINYINT      NOT NULL DEFAULT 0 COMMENT '跳转类型: 0=无跳转 1=SPU详情 2=分类页 3=外链',
    `link_value`   VARCHAR(256) DEFAULT NULL COMMENT '跳转目标值（SPU ID / 分类 ID / URL）',
    `sort`         INT          NOT NULL DEFAULT 0 COMMENT '排序值（升序）',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=显示 1=隐藏',
    `create_time`  BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`  BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页Banner轮播表';

-- 来源: 千语商城 -- 数据库表结构汇总.md
CREATE TABLE IF NOT EXISTS `cms_home_tab` (
    `id`           BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `name`         VARCHAR(64)  NOT NULL COMMENT '显示名称（如"推荐""数码""家用电器"）',
    `tab_key`      VARCHAR(64)  NOT NULL COMMENT '唯一标识（如 recommend/digital/home-appliance）',
    `category_id`  BIGINT       DEFAULT NULL COMMENT '关联分类 ID（pms_category.id，推荐类 Tab 为 NULL）',
    `icon`         VARCHAR(255) DEFAULT NULL COMMENT 'Tab 图标 URL',
    `is_default`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认选中: 0=否 1=是',
    `sort`         INT          NOT NULL DEFAULT 0 COMMENT '排序值（升序）',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=显示 1=隐藏',
    `create_time`  BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`  BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tab_key` (`tab_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页商城Tab配置表';

-- 来源: 千语商城 -- 数据库表结构汇总.md
CREATE TABLE IF NOT EXISTS `cms_zone` (
    `id`                  BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `title`               VARCHAR(64)  NOT NULL COMMENT '区域标题（如"百亿补贴""今日精选"）',
    `tag_text`            VARCHAR(64)  DEFAULT NULL COMMENT '标签文案（如"官方直补""精选上新"）',
    `more_text`           VARCHAR(32)  DEFAULT NULL COMMENT '更多按钮文案（如"更多"）',
    `layout_mode`         VARCHAR(32)  NOT NULL DEFAULT 'double' COMMENT '布局模式: double=双列 quad-card=一行四列',
    `product_count`       INT          NOT NULL DEFAULT 4 COMMENT '展示商品数量（按销量取前 N）',
    `surface_background`  VARCHAR(512) DEFAULT NULL COMMENT '区域背景 CSS（如 linear-gradient 渐变），数据驱动的 UI 样式，运营可配',
    `surface_shadow`      VARCHAR(256) DEFAULT NULL COMMENT '区域阴影 CSS（如 box-shadow），数据驱动的 UI 样式，运营可配',
    `category_id`         BIGINT       DEFAULT NULL COMMENT '关联分类 ID（NULL=全部商品，非 NULL=按分类筛选）',
    `sort`                INT          NOT NULL DEFAULT 0 COMMENT '排序值（升序）',
    `status`              TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=显示 1=隐藏',
    `create_time`         BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time`         BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`             TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页区域/楼层表';


-- ============================================================
-- 全部 41 张表已创建
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- (追加 2026-07-16) CMS 楼层选品改造：手动选品 + 自动投放框架
-- =============================================================================

-- cms_zone 加填充模式列
ALTER TABLE `cms_zone` ADD COLUMN `fill_mode` TINYINT NOT NULL DEFAULT 2
    COMMENT '填充模式: 0=仅手动 1=仅自动 2=手动优先+自动补足' AFTER `category_id`;

-- 楼层-商品关联（手动选品 + 自动投放落点）
CREATE TABLE IF NOT EXISTS `cms_zone_product` (
    `id`          BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `zone_id`     BIGINT       NOT NULL COMMENT '楼层 ID',
    `spu_id`      BIGINT       NOT NULL COMMENT '商品 SPU ID',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '楼层内排序（升序）',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=显示 1=隐藏',
    `source`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=手动 1=自动',
    `rule_id`     BIGINT       DEFAULT NULL COMMENT '自动来源规则 ID（预留）',
    `create_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_zone_spu` (`zone_id`, `spu_id`),
    KEY `idx_zone_sort` (`zone_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼层-商品关联（手动选品+自动投放）';

-- SPU 状态变更流水（驱动楼层自动投放任务）
CREATE TABLE IF NOT EXISTS `pms_spu_status_log` (
    `id`           BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `spu_id`       BIGINT       NOT NULL COMMENT '商品 SPU ID',
    `from_status`  TINYINT      DEFAULT NULL COMMENT '变更前状态',
    `to_status`    TINYINT      NOT NULL COMMENT '变更后状态',
    `event`        VARCHAR(32)  NOT NULL COMMENT '事件: LIST_ON/LIST_OFF/SUBMIT_AUDIT/AUDIT_PASS/AUDIT_REGRESS/EDIT_REGRESS/CREATE',
    `source`       VARCHAR(16)  NOT NULL COMMENT '来源: MERCHANT/ADMIN/SYSTEM',
    `operator_id`  BIGINT       DEFAULT NULL COMMENT '操作者（userId 或 adminId）',
    `reason`       VARCHAR(255) DEFAULT NULL COMMENT '原因（如下架原因）',
    `processed`    TINYINT      NOT NULL DEFAULT 0 COMMENT '0=未消费 1=已消费（投放任务）',
    `process_time` BIGINT       DEFAULT NULL COMMENT '消费时间（毫秒时间戳）',
    `create_time`  BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_spu` (`spu_id`),
    KEY `idx_pending` (`processed`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU 状态变更流水（驱动楼层自动投放）';

-- =============================================================================
-- (追加 2026-07-16) CMS 楼层自动投放规则表
-- =============================================================================
CREATE TABLE IF NOT EXISTS `cms_zone_rule` (
    `id`          BIGINT       NOT NULL COMMENT '主键（Snowflake ID）',
    `zone_id`     BIGINT       NOT NULL COMMENT '楼层 ID',
    `name`        VARCHAR(64)  NOT NULL COMMENT '规则名称',
    `rule_type`   VARCHAR(32)  NOT NULL COMMENT 'NEW_PRODUCT/HIGH_SALES/BY_CATEGORY/KEYWORD',
    `rule_params` JSON                                 COMMENT '规则参数 {threshold/categoryId/days/keyword}',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=启用 1=停用',
    `create_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间（毫秒时间戳）',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_zone_status` (`zone_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼层自动投放规则';
