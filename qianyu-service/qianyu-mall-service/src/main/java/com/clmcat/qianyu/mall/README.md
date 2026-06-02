# qianyu-mall-service 模块说明

> 千语商城业务服务层，包含 11 个业务领域模块。所有模块遵循统一的分包规范。

## 分包规范

```
{domain}/
  controller/   # HTTP 接口层（C端 / B端 / 内部回调）
  mapper/       # MyBatis-Flex Mapper（数据库访问）
  model/        # 数据模型
    dto/        #   请求参数 DTO
    vo/         #   响应视图 VO
    entity/     #   数据库实体
    enums/      #   枚举定义
    status/     #   状态码
  rpc/          # Dubbo RPC 实现（@DubboService，跨服务调用）
  service/      # 内部业务接口
    impl/       #   内部业务实现
  support/      # 辅助工具类
  scheduled/    # 定时任务
```

## 模块总览

| 模块 | 包名 | 用途 | Java 文件数 |
|:----:|------|------|:----------:|
| PMS | `pms` | 商品管理（SPU / SKU / 分类 / 品牌） | 20 |
| OMS | `oms` | 订单管理（下单 / 购物车 / 售后 / 超时自动取消） | 28 |
| MCH | `mch` | 商家管理（入驻 / 店铺 / 账户 / 账单 / 提现 / 运费模板） | 51 |
| PAY | `pay` | 支付管理（微信 / 支付宝支付、异步回调、退款） | 64 |
| LOG | `log` | 物流管理（发货 / 物流轨迹 / 物流公司回调） | 66 |
| INV | `inv` | 库存管理（锁定 / 确认 / 释放 / 调整 / 变更日志） | 20 |
| REV | `rev` | 评价管理（评价提交 / 统计 / 详情页聚合 / 商家回复） | 31 |
| CMS | `cms` | 内容管理（首页 Tab / Banner / 楼层区域） | 22 |
| ADS | `ads` | 收货地址（地址 CRUD / 省市区地区树） | 20 |
| FAV | `fav` | 收藏管理（收藏 / 取消 / 批量状态查询） | 21 |
| HIS | `his` | 历史记录（浏览历史 / 搜索热词） | 16 |

## 模块详情

### PMS — 商品管理 `pms`

商品体系的核心模块，管理 SPU（标准产品单元）、SKU（库存单元）、分类和品牌。

- **C 端**：分类树 / 分类页 / 品牌列表 / 商品搜索 / 商品详情 / SKU 列表
- **B 端**：商品管理页 / 创建编辑 SPU / 上下架 / SKU 批量更新 / 分类 CRUD / 品牌 CRUD
- **数据库表**：`pms_spu`、`pms_sku`、`pms_category`、`pms_brand`、`pms_attribute`、`pms_spu_category`

### OMS — 订单管理 `oms`

订单全生命周期管理，含购物车、售后和超时自动取消。

- **C 端**：下单 / 订单列表详情 / 取消 / 确认收货 / 删除 / 购物车 CRUD / 售后申请
- **B 端**：商家订单列表详情 / 发货 / 售后审批
- **定时任务**：`OmsOrderTimeoutTask` — 每 30 秒扫描超时未支付订单，自动取消并释放库存
- **数据库表**：`oms_order`、`oms_order_item`、`oms_cart`、`oms_after_sale`

### MCH — 商家管理 `mch`

商家入驻、店铺运营、资金管理的核心模块，是 B 端功能最密集的领域。

- **C 端**：店铺首页 / 店铺商品列表 / 店铺首页聚合
- **B 端**：商家仪表盘 / 入驻申请与审核 / 店铺信息管理 / 账户余额 / 账单流水 / 结算 / 提现 / 运费模板 CRUD
- **数据库表**：`mch_merchant`、`mch_store`、`mch_account`、`mch_bill`、`mch_settlement_info`、`mch_withdrawal`、`mch_merchant_cert`、`mch_freight_template`、`mch_freight_rule`

### PAY — 支付管理 `pay`

对接微信支付和支付宝，处理支付申请、异步回调通知和退款。

- **C 端**：发起支付 / 支付结果查询
- **回调**：微信支付通知 / 支付宝通知 / 微信退款通知 / 支付宝退款通知
- **内部**：退款接口
- **数据库表**：`pay_payment`、`pay_refund`

### LOG — 物流管理 `log`

发货与物流轨迹管理，支持物流公司状态推送回调。

- **C 端**：物流查询 / 物流轨迹
- **B 端**：创建物流单 / 更新物流信息
- **回调**：物流公司状态推送
- **数据库表**：`log_shipping`、`log_delivery_trace`

### INV — 库存管理 `inv`

库存锁定与释放机制，基于乐观锁保证并发安全。

- **内部接口**：锁定库存 / 确认库存 / 释放库存 / 批量查询
- **B 端**：库存调整 / 库存变更日志
- **数据库表**：`inv_stock`、`inv_stock_log`

### REV — 评价管理 `rev`

商品评价体系，支持用户提交评价、评价统计聚合和商家回复。

- **C 端**：商品评价列表 / 评价统计 / 评价详情页聚合 / 提交评价 / 我的评价
- **B 端**：商家评价列表 / 回复评价
- **数据库表**：`rev_review`、`rev_review_stat`

### CMS — 内容管理 `cms`

商城首页内容运营，管理 Tab 导航、轮播图和楼层区域。

- **C 端**：首页聚合数据（Tab + Banner + Zone 楼层）
- **RPC**：管理员设置默认 Tab
- **数据库表**：`cms_home_tab`、`cms_banner`、`cms_zone`
- **特性**：Tab 和 Banner 使用 volatile + @Scheduled 定时刷新缓存到内存

### ADS — 收货地址 `ads`

用户收货地址管理和省市区三级联动地区数据。

- **C 端**：地址 CRUD / 设置默认地址 / 地区树查询
- **数据库表**：`ads_address`、`ads_region`

### FAV — 收藏管理 `fav`

用户收藏功能，支持商品和店铺的收藏/取消及批量状态查询。

- **C 端**：添加收藏 / 取消收藏 / 收藏列表 / 收藏状态查询 / 批量状态 / 批量取消
- **数据库表**：`fav_favorite`

### HIS — 历史记录 `his`

用户行为记录，包括浏览历史和搜索热词统计。

- **C 端**：浏览历史列表 / 记录浏览 / 删除历史 / 搜索热词 / 记录搜索关键词
- **数据库表**：`his_browse_history`、`his_search_keyword`

## 接口路径规范

| 前缀 | 用途 |
|------|------|
| `/api/mall/{domain}/` | C 端接口（面向消费者） |
| `/api/mall/merchant/{domain}/` | B 端接口（面向商家） |
| `/api/mall/internal/{domain}/` | 内部接口（服务间调用） |
| `/api/mall/callback/{domain}/` | 外部回调（支付/物流通知） |
