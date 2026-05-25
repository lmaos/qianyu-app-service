# QIANYU 服务器

状态: 开发中

## 项目结构: 

```X
qianyu-api: 对外暴露的接口
qianyu-core: 公共层，所有Service 依赖。
qianyu-service: 业务模块
qianyu-boot: 启动模块

```

### 依赖关系:

```X

启动依赖: qianyu-boot -> qianyu-service -> qianyu-core

调用依赖: qianyu-service -> qianyu-api

```

### 启动模块说明

```
qianyu-boot: 负责启动应用.

qianyu-single-boot: 单体应用启动模块. 依赖全部Service模块， 全部启动.

qianyu-{module}-boot: 模块化启动模块. 依赖单个Service模块， 启动单个模块.


```

### 模块包说明: 

```X
qianyu-api/
├── qianyu-core-api/ # 公共 API（基础DTO、工具类、异常定义等）
├── qianyu-user-api/ # 用户服务 API
├── qianyu-social-api/ # 社交动态服务 API
├── qianyu-live-api/ # 直播服务 API
├── qianyu-gift-api/ # 礼物/虚拟资产服务 API
├── qianyu-im-api/ # IM消息服务 API
├── qianyu-mail-api/ # 商城服务 API
├── qianyu-storage-api/ # 存储服务 API
├── qianyu-payment-api/ # 支付服务 API
├── qianyu-notification-api/ # 通知服务 API
├── qianyu-review-api/ # 内容审核服务 API
qianyu-service/
├── qianyu-user-service/ # 用户服务实现
├── qianyu-social-service/ # 社交动态服务实现
├── qianyu-live-service/ # 直播服务实现
├── qianyu-gift-service/ # 礼物服务实现
├── qianyu-im-service/ # IM服务调用实现
├── qianyu-mail-service/ # 商城服务实现
├── qianyu-storage-service/ # 存储服务实现
├── qianyu-payment-service/ # 支付服务实现
├── qianyu-notification-service/# 通知服务实现
└── qianyu-review-service/ # 内容审核服务实现
```
## 项目框架使用: 

```X
clmcat-framework: 轻量级微服务框架，提供基础设施和公共组件支持。
```