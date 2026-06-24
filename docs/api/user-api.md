# 用户信息 API 文档

> 面向前端接入 / AI 参考。覆盖用户信息查询、修改相关 HTTP 接口。

## 目录

1. [通用说明](#1-通用说明)
2. [读取当前登录用户 ID](#2-读取当前登录用户-id)
3. [查询单个用户信息](#3-查询单个用户信息)
4. [批量查询用户信息](#4-批量查询用户信息)
5. [查询当前登录用户信息](#5-查询当前登录用户信息)
6. [按 userNo 搜索用户](#6-按-userno-搜索用户)
7. [修改当前登录用户信息](#7-修改当前登录用户信息)
8. [公共错误码](#8-公共错误码)
9. [附录：对象定义](#9-附录对象定义)

---

## 1. 通用说明

### 1.1 认证

所有接口需要登录态，在 **HTTP Header** 中携带 token：

```
token: eyJhbGciOiJSUzI1NiJ9...
```

Token 获取方式见登录相关文档。

### 1.2 统一响应结构

所有接口使用 `@ApiController` 注解，响应统一封装为：

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
| status | int | 状态码，`0` 表示成功，非 `0` 表示失败 |
| state | String | 状态文本，与 status 对应 |
| content | Object | 业务数据（各接口返回的具体 VO） |
| message | String | 附加说明信息 |

详细约定见 [api-struct.md](../api-struct.md)。

### 1.3 接口前缀

```
/api/user
```

所有接口按语义使用 `GET` 或 `POST`。Token 通过 Header `token` 传递。

### 1.4 缓存说明

| 查询场景 | 缓存策略 |
|----------|----------|
| 查自己（targetId == userId） | 直接查 DB，不走缓存，返回完整资料（含 phone、email） |
| 查他人 | Caffeine 本地缓存（2 分钟 TTL，最大 2048 条），返回公开资料 |

---

## 2. 读取当前登录用户 ID

验证 token 注入是否生效，调试用。

### 2.1 接口定义

```
GET /api/user/value
```

### 2.2 请求参数

无（userId 由 Header `token` 自动解析）。

### 2.3 响应

`content` 为字符串 `"user:{userId}"`。

### 2.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": "user:2495058814603264",
  "message": "OK"
}
```

---

## 3. 查询单个用户信息

根据 `targetId` 查询指定用户的基础信息。

### 3.1 接口定义

```
GET /api/user/user_info/get?targetId={userId}
```

### 3.2 请求参数

**UserIdDto**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetId | Long | 是 | 目标用户 ID |

```json
{
  "targetId": 2495058814603265
}
```

### 3.3 响应参数

`content` 为 [UserInfoVo](#91-user_infovo) 对象；用户不存在时返回 `null`。

### 3.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userNo": "O4oUzVm2j",
    "userId": 2495058814603265,
    "nickname": "okfa71pdz4",
    "avatar": "https://cdn.example.com/avatar.jpg",
    "bio": "这是我的个人简介",
    "gender": 0,
    "birthday": "1995-06-15",
    "age": 31,
    "phone": null,
    "phoneVerifiedTime": null,
    "email": null,
    "country": "CN",
    "province": "广东省",
    "city": "深圳市",
    "lastLoginTime": 1719123456,
    "status": 0,
    "freezeEndTime": 0,
    "createTime": 1719000000,
    "updateTime": 1719123456
  },
  "message": "OK"
}
```

> **注意**：查他人时 `phone`、`email` 等敏感字段返回 `null`；查自己时返回完整值。

### 3.5 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | targetId 为空或 <= 0 |
| 100003 | 403 | token 失效或未传 |

---

## 4. 批量查询用户信息

根据 ID 数组或逗号分隔字符串批量查询，按请求顺序返回。

### 4.1 接口定义

```
GET /api/user/user_info/list
```

### 4.2 请求参数

**UserIdsDto**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetIds | List\<Long\> | 否 | JSON 数组传参 |
| targetIdsText | String | 否 | 逗号分隔的 ID 字符串（兼容 query/form），如 `1,2,3` |

> `targetIds` 和 `targetIdsText` 至少填一个，同时填写时合并去重。

```json
{
  "targetIds": [2495058814603264, 2495058814603265]
}
```

query 方式：

```
GET /api/user/user_info/list?targetIdsText=2495058814603264,2495058814603265
```

### 4.3 响应参数

`content` 为 `List<`[UserInfoVo](#91-user_infovo)`>`，按请求中的 ID 顺序返回。

### 4.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": [
    {
      "userNo": "O4oUzVm2j",
      "userId": 2495058814603264,
      "nickname": "用户A",
      "avatar": "https://cdn.example.com/a.jpg",
      "bio": "",
      "gender": 1,
      "birthday": null,
      "age": null,
      "country": "CN",
      "province": "北京",
      "city": "朝阳区"
    },
    {
      "userNo": "Xk9mPq2Rt",
      "userId": 2495058814603265,
      "nickname": "用户B",
      "avatar": "https://cdn.example.com/b.jpg",
      "bio": "Hello",
      "gender": 2,
      "birthday": "1990-01-01",
      "age": 36
    }
  ],
  "message": "OK"
}
```

### 4.5 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | targetIds 和 targetIdsText 均为空 |
| 100003 | 403 | token 失效或未传 |

---

## 5. 查询当前登录用户信息

查询当前登录用户自己的完整资料，不走缓存，返回敏感字段。

### 5.1 接口定义

```
GET /api/user/user_info/self
```

### 5.2 请求参数

无（userId 由 Header `token` 自动解析）。

### 5.3 响应参数

`content` 为 [UserInfoVo](#91-user_infovo) 对象，含完整字段（phone、email 等均返回）。

### 5.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userNo": "O4oUzVm2j",
    "userId": 2495058814603264,
    "nickname": "okfa71pdz4",
    "avatar": "https://cdn.example.com/avatar.jpg",
    "bio": "",
    "gender": 0,
    "birthday": null,
    "age": null,
    "phone": "+86-13800138000",
    "phoneVerifiedTime": 1719000000,
    "email": "user@example.com",
    "country": "CN",
    "province": null,
    "city": null,
    "lastLoginTime": 1719123456,
    "status": 0,
    "freezeEndTime": 0,
    "createTime": 1719000000,
    "updateTime": 1719123456
  },
  "message": "OK"
}
```

---

## 6. 按 userNo 搜索用户

用于添加好友等场景的精确搜索。userNo 是用户外显 ID，全局唯一。

### 6.1 接口定义

```
GET /api/user/user_info/search?userNo={userNo}
```

### 6.2 请求参数

**UserNoSearchDto**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userNo | String | 是 | 用户外显 ID，全局唯一，6-20 位字母数字组合 |

```json
{
  "userNo": "O4oUzVm2j"
}
```

### 6.3 响应参数

`content` 为 [UserInfoVo](#91-user_infovo) 对象；未命中返回 `null`。

### 6.4 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | userNo 为空 |
| 100003 | 403 | token 失效或未传 |

---

## 7. 修改当前登录用户信息

修改当前登录用户自己的个人资料。支持增量更新，只传需要修改的字段即可。

### 7.1 接口定义

```
POST /api/user/user_info/update
```

### 7.2 请求参数

**UserInfoUpdateDto**（JSON Body）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | String | 否 | 昵称，最长 64 字符，支持表情符号 |
| avatar | String | 否 | 头像 URL |
| bio | String | 否 | 个人简介，最长 1000 字符 |
| gender | Integer | 否 | 性别：`0` 未知，`1` 女性，`2` 男性 |
| birthday | String | 否 | 生日，格式 `yyyy-MM-dd`（如 `1990-01-01`） |
| country | String | 否 | 国家/地区，ISO 3166-1 alpha-2 代码（如 `CN`、`US`） |
| province | String | 否 | 省份/州，自由文本 |
| city | String | 否 | 城市，自由文本 |

### 7.3 请求示例

```json
{
  "nickname": "我的新昵称",
  "bio": "这是一段个人简介",
  "gender": 1,
  "birthday": "1995-06-15",
  "country": "CN",
  "province": "广东省",
  "city": "深圳市"
}
```

### 7.4 响应参数

`content` 为修改后的 [UserInfoVo](#91-user_infovo) 完整对象。

### 7.5 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userNo": "O4oUzVm2j",
    "userId": 2495058814603264,
    "nickname": "我的新昵称",
    "avatar": "https://cdn.example.com/avatar.jpg",
    "bio": "这是一段个人简介",
    "gender": 1,
    "birthday": "1995-06-15",
    "age": 31,
    "phone": "+86-13800138000",
    "phoneVerifiedTime": 1719000000,
    "email": "user@example.com",
    "country": "CN",
    "province": "广东省",
    "city": "深圳市",
    "lastLoginTime": 1719123456,
    "status": 0,
    "freezeEndTime": 0,
    "createTime": 1719000000,
    "updateTime": 1719200000
  },
  "message": "OK"
}
```

### 7.6 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | 所有字段均为空，没有可更新的内容 |
| 100003 | 403 | token 失效或未传 |

---

## 8. 公共错误码

以下错误码在所有用户信息接口中通用：

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 0 | 200 | OK | 成功 |
| 300004 | 400 | P_VALUE_ERROR | 参数值校验不通过（userId <= 0、必填参数缺失等） |
| 100001 | 401 | AUTH_NOT_LOGIN | 未登录 |
| 100003 | 403 | AUTH_TOKEN_INVALID | token 失效或未传 |
| 400001 | 422 | R_ERROR | 业务异常（通用） |
| 400002 | 422 | R_OPERATION_FAIL | 操作失败 |
| 500 | 500 | SYSTEM_ERROR | 系统内部错误 |

---

## 9. 附录：对象定义

### 9.1 UserInfoVo

用户基础信息返回对象，用于所有信息查询和修改接口的响应。

| 字段 | 类型 | 说明 |
|------|------|------|
| userNo | String | 用户外显编号（全局唯一，6-20 位字母数字） |
| userId | Long | 用户 ID（业务主键，雪花 ID） |
| nickname | String | 昵称，最长 64 字符 |
| avatar | String | 头像 URL |
| bio | String | 个人简介，最长 1000 字符 |
| gender | Integer | 性别：`0` 未知，`1` 女性，`2` 男性 |
| birthday | String | 生日，格式 `yyyy-MM-dd` |
| age | Integer | 年龄（冗余字段，由 birthday 计算） |
| phone | String | 手机号（查他人时为 `null`） |
| phoneVerifiedTime | Long | 手机号认证通过时间戳（Unix 秒） |
| email | String | 邮箱（查他人时为 `null`） |
| country | String | 国家/地区代码（ISO 3166-1 alpha-2） |
| province | String | 省份/州 |
| city | String | 城市 |
| lastLoginTime | Long | 最后登录时间戳（Unix 秒） |
| status | Integer | 账号状态：`0` 正常，`1` 冻结，`2` 注销 |
| freezeEndTime | Long | 冻结结束时间戳（Unix 秒），`0` 表示未冻结 |
| createTime | Long | 创建时间戳（Unix 秒） |
| updateTime | Long | 更新时间戳（Unix 秒） |

### 9.2 完整调用流程

```bash
# ===== 1. 查自己的完整资料 =====
curl -X GET http://localhost:8080/api/user/user_info/self \
  -H "token: <token>"

# ===== 2. 查单个用户 =====
curl -X GET "http://localhost:8080/api/user/user_info/get?targetId=2495058814603265" \
  -H "token: <token>"

# ===== 3. 批量查用户 =====
curl -X GET "http://localhost:8080/api/user/user_info/list?targetIdsText=2495058814603264,2495058814603265" \
  -H "token: <token>"

# ===== 4. 按 userNo 搜索 =====
curl -X GET "http://localhost:8080/api/user/user_info/search?userNo=O4oUzVm2j" \
  -H "token: <token>"

# ===== 5. 修改个人资料 =====
curl -X POST http://localhost:8080/api/user/user_info/update \
  -H "Content-Type: application/json" \
  -H "token: <token>" \
  -d '{"nickname":"新昵称","bio":"新简介","gender":1}'
```

### 9.3 服务模块

| 模块 | 说明 |
|------|------|
| `qianyu-user-service` | 用户服务实现（UserController + UserViewServiceBiz + UserServiceBiz） |
| `qianyu-user-api` | 用户 Dubbo RPC 接口（UserApi，供其他微服务调用） |

### 9.4 注解说明

| 注解 | 作用 |
|------|------|
| `@ApiController` | 统一响应封装（将返回值包入 `content`） + 全局异常处理 |
| `@LoginVerify` | 当前类所有接口需要登录态（Header 携带有效 token） |
| `@Token` | 自动从 token 中解析 userId 注入方法参数 |
| `@Params` | 声明参数来源（query/form/body 自动绑定） |
