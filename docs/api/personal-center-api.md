# 个人中心 API 文档

## 目录

1. [通用说明](#1-通用说明)
2. [获取个人中心数据](#2-获取个人中心数据)
3. [分页查询内容列表](#3-分页查询内容列表)
4. [公共错误码](#4-公共错误码)
5. [附录：对象定义](#5-附录对象定义)

---

## 1. 通用说明

### 1.1 认证

所有接口需要登录态，在 **HTTP Header** 中携带 token：

```
token: eyJhbGciOiJSUzI1NiJ9...
```

Token 获取方式：`POST /api/user/login/phone`，见 [login-phone-api.md](login-phone-api.md)。

### 1.2 统一响应结构

```json
{
  "requestId": "xxx",
  "status": 0,
  "state": "OK",
  "content": {},
  "message": "OK"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| requestId | String | 请求唯一标识 |
| status | int | `0` 成功，非 `0` 失败 |
| state | String | 状态码枚举名 |
| content | Object | 业务数据 |
| message | String | 说明 |

### 1.3 接口前缀

```
GET /api/app
```

所有接口均使用 `GET` 方法。Token 通过 Header `token` 传递。

---

## 2. 获取个人中心数据

一次性返回用户基础信息、统计数据、快捷入口。

### 2.1 接口定义

```
GET /api/app/personal/center
```

### 2.2 请求参数

无请求参数（userId 由 Header `token` 自动解析）。

### 2.3 响应参数

**PersonalCenterDto**

```
content
├── userProfile   UserProfileDto   用户基础信息
├── userStats     UserStatsDto     统计数据
└── shortcuts     List<ShortcutDto> 快捷入口列表
```

**UserProfileDto**

| 字段 | 类型 | 说明 |
|------|------|------|
| avatar | String | 头像 URL |
| nickname | String | 昵称 |
| userNo | String | 用户外显编号 |
| signature | String | 个人签名 |
| location | String | 所在地（`省份-城市` 格式，如 "广东省-深圳市-南山区"） |

**UserStatsDto**

| 字段 | 类型 | 说明 |
|------|------|------|
| likeCount | Long | 获赞数 |
| followCount | Long | 关注数 |
| fansCount | Long | 粉丝数 |
| visitorCount | Long | 新访客数（当前返回 0，TODO 后续接入埋点） |

**ShortcutDto**

| 字段 | 类型 | 说明 |
|------|------|------|
| key | String | 入口标识 |
| name | String | 入口名称 |
| visible | Boolean | 是否可见 |
| badgeCount | Long | 角标数量（如未读消息数） |
| linkUrl | String | 跳转链接 |

### 2.4 响应示例

```json
{
  "requestId": "322655903800561664",
  "status": 0,
  "state": "OK",
  "content": {
    "userProfile": {
      "avatar": null,
      "nickname": "1frhqe3in0h",
      "userNo": "O4oUzVm2j",
      "signature": "",
      "location": ""
    },
    "userStats": {
      "likeCount": 0,
      "followCount": 0,
      "fansCount": 0,
      "visitorCount": 0
    },
    "shortcuts": [
      {
        "key": "anchor",
        "name": "主播中心",
        "visible": true,
        "badgeCount": 0,
        "linkUrl": "page://open?page=/pages/user/anchor-center"
      },
      {
        "key": "merchant",
        "name": "商家管理",
        "visible": true,
        "badgeCount": 0,
        "linkUrl": "page://open?page=/pages/user/merchant-center"
      },
      {
        "key": "orders",
        "name": "订单中心",
        "visible": true,
        "badgeCount": 0,
        "linkUrl": "page://open?page=/pages/user/order-list"
      },
      {
        "key": "wallet",
        "name": "我的钱包",
        "visible": true,
        "badgeCount": 0,
        "linkUrl": "page://open?page=/pages/user/wallet"
      }
    ]
  },
  "message": "OK"
}
```

### 2.5 快捷入口说明

完整注册表（`ShortcutConfig`），前端支持的全部 key：

| key | name | linkUrl | 本期返回 |
|-----|------|---------|----------|
| anchor | 主播中心 | `page://open?page=/pages/user/anchor-center` | ✅ |
| merchant | 商家管理 | `page://open?page=/pages/user/merchant-center` | ✅ |
| orders | 订单中心 | `page://open?page=/pages/user/order-list` | ✅ |
| wallet | 我的钱包 | `page://open?page=/pages/user/wallet` | ✅ |
| friends | 添加朋友 | `page://open?page=/pages/user/add-friend` | ❌ |
| visitors | 新访客 | `page://open?page=/pages/user/visitor-list` | ❌ |
| settings | 更多设置 | `page://open?page=/pages/user/more-settings` | ❌ |
| edit | 编辑主页 | `page://open?page=/pages/user/edit-profile` | ❌ |
| all-functions | 全部功能 | `page://open?page=/pages/user/all-functions` | ❌ |

> 当前通过 `ShortcutService` 接口 + `ShortcutServiceDefaultImpl` 写死返回前 4 项，`linkUrl` 从 `ShortcutConfig.LINK_URLS` 静态读取。后续替换实现即可动态查询各子服务的可见性和角标。

### 2.6 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | userId 为 0 或负数 |
| 100003 | 403 | token 失效或未传 |

---

## 3. 分页查询内容列表

按 Tab 类型分页查询用户内容。

### 3.1 接口定义

```
GET /api/app/personal/center/contents
```

### 3.2 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| tab | String | 是 | — | 内容类型：`moment` / `work` / `like` / `history` |
| cursor | Long | 否 | `0` | 游标（上一页最后一条的 ID），首次传 `0` |
| limit | int | 否 | `20` | 分页大小，范围 1-50 |

### 3.3 Tab 类型说明

| tab | 含义 | 数据来源 |
|-----|------|---------|
| moment | 用户发布的动态（图文/视频混合） | moment 表按 author_id 查询 |
| work | 用户的作品（仅视频） | moment 表按 author_id 查询后过滤 type=video |
| like | 用户点赞的内容 | moment_like 表 + 关联 moment 详情 |
| history | 用户浏览历史 | TODO 未上线，始终返回空 |

### 3.4 响应参数

**ContentPageDto**

| 字段 | 类型 | 说明 |
|------|------|------|
| items | List\<ContentTabDto\> | 内容列表 |
| nextCursor | Long | 下一页游标，传 0 表示没有下一页 |
| hasMore | Boolean | 是否还有更多数据 |

**ContentTabDto**

| 字段 | 类型 | 说明 |
|------|------|------|
| momentId | Long | 作品 ID，用于跳转详情 |
| coverUrl | String | 封面图 URL（视频取 coverUrl，图文取第一张图） |
| title | String | 标题/描述文本 |
| type | String | 内容类型：`video` / `image` / `text` |
| commentCount | Long | 评论数 |
| likeCount | Long | 点赞数 |
| viewCount | Long | 观看数（当前返回 0，TODO 后续接入播放统计） |

### 3.5 响应示例

```json
{
  "requestId": "322655903800561665",
  "status": 0,
  "state": "OK",
  "content": {
    "items": [
      {
        "momentId": 5257117397155841,
        "coverUrl": "https://cdn.example.com/cover.jpg",
        "title": "记录今天的日落 🌇",
        "type": "video",
        "commentCount": 3,
        "likeCount": 15,
        "viewCount": 0
      },
      {
        "momentId": 5257117397155840,
        "coverUrl": "https://cdn.example.com/img1.jpg",
        "title": "",
        "type": "image",
        "commentCount": 0,
        "likeCount": 5,
        "viewCount": 0
      }
    ],
    "nextCursor": 5257117397155840,
    "hasMore": false
  },
  "message": "OK"
}
```

### 3.6 游标分页说明

- `cursor=0` 表示从头开始，查询最新一页
- 响应中的 `nextCursor` 作为下一页的 `cursor` 传入
- `hasMore=false` 且 `nextCursor=0` 表示已到最后一页
- 每页最多 50 条，默认 20 条

### 3.7 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | tab 为空、userId 为 0 |
| 400020 | 422 | tab 值不在 `moment/work/like/history` 范围内 |

---

## 4. 公共错误码

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 0 | 200 | OK | 成功 |
| 300004 | 400 | P_VALUE_ERROR | 参数值校验不通过 |
| 100003 | 403 | AUTH_TOKEN_INVALID | token 失效 |
| 400001 | 422 | R_ERROR | 业务异常（通用） |
| 400002 | 422 | R_OPERATION_FAIL | 操作失败 |
| 400020 | 422 | R_NOT_SUPPORTED | 不支持的 tab 类型 |
| 500 | 500 | SYSTEM_ERROR | 系统内部错误 |

---

## 5. 附录：对象定义

### 5.1 完整请求示例

```bash
# 1. 登录获取 token
curl -X POST http://localhost:8080/api/user/login/phone \
  -H "Content-Type: application/json" \
  -d '{"phone":"+86-13800138000","code":"123456"}'

# 2. 获取个人中心数据
curl -X GET http://localhost:8080/api/app/personal/center \
  -H "token: <token_from_step1>"

# 3. 获取动态列表（第1页）
curl -X GET "http://localhost:8080/api/app/personal/center/contents?tab=moment&cursor=0&limit=10" \
  -H "token: <token_from_step1>"

# 4. 获取作品列表（仅视频）
curl -X GET "http://localhost:8080/api/app/personal/center/contents?tab=work&cursor=0&limit=10" \
  -H "token: <token_from_step1>"

# 5. 获取喜欢列表
curl -X GET "http://localhost:8080/api/app/personal/center/contents?tab=like&cursor=0&limit=10" \
  -H "token: <token_from_step1>"
```

### 5.2 测试账号

| 属性 | 值 |
|------|-----|
| phone | `+86-13800138000` |
| code | `123456` |

### 5.3 服务模块

| 模块 | 说明 |
|------|------|
| `qianyu-center-api` | 个人中心聚合 API 接口定义 |
| `qianyu-center-service` | 个人中心聚合服务实现（PersonalCenterController + PersonalCenterServiceBiz） |
| `qianyu-user-service` | 用户基础信息服务 |
| `qianyu-social-service` | 社交统计、关注、点赞、动态、访客服务 |

### 5.4 快捷入口扩展说明

`ShortcutConfig.REGISTRY` 维护全部入口 key→name 映射，`ShortcutService` 接口决定返回哪些及各自的 `visible`/`badgeCount`/`linkUrl`。

当前实现 `ShortcutServiceDefaultImpl` 写死返回 4 项（anchor / merchant / orders / wallet），后续接入子服务时替换实现即可：

| 入口 key | 来源服务 | 判断逻辑 |
|----------|---------|---------|
| anchor | qianyu-live-service | 查询用户是否为主播 |
| merchant | qianyu-mall-service | MerchantApi.getByUserId() 是否已认证 |
| orders | qianyu-mall-service | OmsOrderApi 查询是否有进行中订单 |
| wallet | qianyu-payment-service | 查询账户余额 |
| friends | qianyu-social-service | 好友/关注推荐 |
| visitors | qianyu-social-service | 访客记录 |
| settings | — | 始终可见 |
| edit | — | 始终可见（本人主页） |
