# 手机号登录 API 文档

## 目录

1. [通用说明](#1-通用说明)
2. [发送手机号验证码](#2-发送手机号验证码)
3. [手机号登录](#3-手机号登录)
4. [公共错误码](#4-公共错误码)
5. [完整调用流程](#5-完整调用流程)
6. [附录：参数对象定义](#6-附录参数对象定义)

---

## 1. 通用说明

### 1.1 统一响应结构

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
| content | Object | 业务数据（本章接口返回的具体 VO） |
| message | String | 附加说明信息 |

详细约定见 [docs/api-struct.md](../api-struct.md)。

### 1.2 HTTP 状态码含义

| HTTP 状态码 | 含义 | 场景 |
|-------------|------|------|
| 200 | 成功 | 请求正常处理 |
| 400 | 参数错误 (P_ERROR) | 必填参数缺失、格式错误 |
| 401 | 登录失败 (U_ERROR) | 验证码错误、账户不存在等登录态相关 |
| 422 | 业务错误 (R_ERROR) | 数据不存在、操作频繁等业务逻辑 |
| 429 | 限流 (L_ERROR) | 请求过于频繁被限制 |
| 500 | 系统错误 (S_ERROR) | 服务端未预期异常 |

### 1.3 接口前缀

```
POST /api/user/login
```

所有接口均使用 `POST` 方法，请求体以 `application/json` 格式提交。

---

## 2. 发送手机号验证码

发送短信验证码到指定手机号。

### 2.1 接口定义

```
POST /api/user/login/phone/verify_code
```

### 2.2 请求参数

**PhoneVerifyDto**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 是 | 手机号，**要求带国家码**，例如 `+86-13800000000` |
| secondVerifyCode | String | 否 | 预留的二次校验码，当前未参与校验 |

```json
{
  "phone": "+86-13800000000",
  "secondVerifyCode": ""
}
```

### 2.3 响应参数

**PhoneVerifyVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| needSecondVerify | boolean | 是否需要进行二次校验（预留字段） |

```json
{
  "requestId": "a1b2c3d4",
  "status": 0,
  "state": "OK",
  "content": {
    "needSecondVerify": false
  },
  "message": "OK"
}
```

### 2.4 业务说明

| 要点 | 说明 |
|------|------|
| 验证码有效期 | 由服务端配置（通常 5 分钟），过期自动失效 |
| 发送频率 | 同一手机号在指定时间间隔内仅能发送一次，超限返回限流错误 |
| 验证码校验 | 发送成功后，调用方需将验证码传递给 [手机号登录 API](#3-手机号登录)，由服务端一次性校验 |
| phone 格式 | 必须为 `+86-xxxxxxxxxxx` 格式：以 `+86` 开头，通过 `LoginSupport.isValidTelephone()` 校验（`+` 开头、含 `-` 分隔符），且号码本身为中国大陆有效手机号。`+86` 以外的国家码暂不支持短信发送 |

### 2.5 常见错误

| 错误码 | HTTP 状态 | 说明 |
|--------|-----------|------|
| 300004 | 400 | phone 参数为空或格式错误 |
| 400005 | 429 | 同一手机号发送过于频繁（频率限制） |
| 500003 | 503 | 短信服务商不可用（下游服务异常） |

---

## 3. 手机号登录

使用手机号 + 验证码（或密码）登录。如果手机号尚未注册，会自动完成注册并返回登录结果。

### 3.1 接口定义

```
POST /api/user/login/phone
```

### 3.2 请求参数

**PhoneLoginDto**

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| phone | String | 是 | — | 手机号，**必须带国家码**，`+XX-xxxxx` 格式，如 `+86-13800000000` |
| code | String | 条件必填 | — | 短信验证码。`authMode=CODE` 时必须 |
| password | String | 条件必填 | — | 登录密码。`authMode=PASSWORD` 时必须 |
| authMode | String | 否 | `CODE` | 验证模式：`CODE` — 短信验证码登录；`PASSWORD` — 密码登录 |
| clientIp | String | 否 | — | 客户端 IP，用于安全审计 |

```json
{
  "phone": "+86-13800000000",
  "code": "123456",
  "authMode": "CODE",
  "clientIp": "192.168.1.1"
}
```

**密码登录模式示例：**

```json
{
  "phone": "+86-13800000000",
  "password": "myPassword123",
  "authMode": "PASSWORD",
  "clientIp": "192.168.1.1"
}
```

### 3.3 响应参数

**LoginResultVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | 登录签名 token，后续请求需要携带此 token |
| userInfo | UserInfoVo | 用户基本信息（见下方） |

**UserInfoVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| userNo | String | 用户外显编号（用户个人主页等公开场景使用） |
| userId | Long | 用户内部 ID |
| nickname | String | 昵称 |
| avatar | String | 头像 URL |
| bio | String | 个人简介 |
| gender | Integer | 性别：`0`-未知，`1`-女性，`2`-男性 |
| birthday | LocalDate | 生日 |
| age | Integer | 年龄 |
| phone | String | 手机号 |
| phoneVerifiedTime | Long | 手机号认证通过的时间戳（ms） |
| email | String | 邮箱 |
| country | String | 国家/地区代码 |
| province | String | 省份/州 |
| city | String | 城市 |
| lastLoginTime | Long | 最后登录时间戳（ms） |
| status | Integer | 账号状态 |
| freezeEndTime | Long | 冻结结束时间戳（ms），未冻结时为 `null` |
| createTime | Long | 创建时间戳（ms） |
| updateTime | Long | 更新时间戳（ms） |

```json
{
  "requestId": "a1b2c3d4",
  "status": 0,
  "state": "OK",
  "content": {
    "token": "eyJhbGciOiJSUzI1NiJ9...",
    "userInfo": {
      "userNo": "ABC123",
      "userId": 100001,
      "nickname": "用户_100001",
      "avatar": null,
      "bio": "",
      "gender": 0,
      "birthday": null,
      "age": 0,
      "phone": "+86-13800000000",
      "phoneVerifiedTime": 1718000000000,
      "email": null,
      "country": "CN",
      "province": null,
      "city": null,
      "lastLoginTime": null,
      "status": 0,
      "freezeEndTime": null,
      "createTime": 1718000000000,
      "updateTime": 1718000000000
    }
  },
  "message": "OK"
}
```

### 3.4 业务说明

| 要点 | 说明 |
|------|------|
| 自动注册 | 如果 `phone` 尚不存在于 `user_auth` 表中，会自动创建一条新用户记录并返回登录结果 |
| 验证码校验 | `authMode=CODE` 时，服务端会校验 `code` 参数是否与之前通过 `/phone/verify_code` 发送的验证码一致 |
| 密码登录 | `authMode=PASSWORD` 时，校验 `password` 与数据库中存储的 credential，**不需要** `code` |
| 手机号格式校验 | `phone` 必须通过 `LoginSupport.isValidTelephone()` 校验：以 `+` 开头、包含 `-` 分隔符（如 `+86-13800000000`），且通过 libphonenumber 格式验证 |
| phone 直接存储 | 校验通过的 `phone` 原样存入 `user_auth.identifier`（含国家码和分隔符），不再标准化为纯数字 |
| token 签发 | 登录成功后签发 RSA 签名 token（RS256），后续请求需在 Header 携带此 token |

### 3.5 常见错误

| 错误码 | HTTP 状态 | 说明 |
|--------|-----------|------|
| 300004 | 400 | 参数为空或格式不正确（phone 必须符合 `+XX-xxxxx` 格式、password 长度 6-64） |
| 100002 | 401 | 登录失败：验证码不正确、手机号格式不合法、密码错误、或账户不存在 |
| 200003 | 401 | 注册失败：该手机号已使用账号密码注册（仅密码模式不涉及自动注册时） |
| 200005 | 401 | 账户被冻结，不允许登录 |
| 400003 | 422 | 用户数据不存在（`user_info` 记录丢失，罕见） |
| 400005 | 429 | 操作过于频繁 |
| 400020 | 422 | 不支持的 authMode 值 |

---

## 4. 公共错误码

以下错误码在所有登录相关接口中通用：

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 0 | 200 | OK | 处理成功 |
| 300001 | 400 | P_ERROR | 参数错误（通用） |
| 300002 | 400 | P_NOTNULL | 参数值不可为空 |
| 300004 | 400 | P_VALUE_ERROR | 参数值校验不通过 |
| 100002 | 401 | AUTH_LOGIN_FAIL | 登录失败（验证码/密码错误） |
| 200001 | 401 | U_INFO_ERROR | 用户信息错误 |
| 200005 | 401 | U_FREEZE | 账户被冻结 |
| 400001 | 422 | R_ERROR | 业务异常（通用） |
| 400002 | 422 | R_OPERATION_FAIL | 操作失败 |
| 400003 | 422 | R_NOEXIST_DATA | 数据不存在 |
| 400005 | 429 | L_FREQUENT_ACCESS | 访问过于频繁 |
| 500 | 500 | SYSTEM_ERROR | 系统内部错误 |
| 500003 | 503 | F_SERVICE_UNAVAILABLE | 下游服务不可用 |

---

## 5. 完整调用流程

### 5.1 验证码登录流程

```
┌──────────┐      ┌──────────────────┐      ┌─────────────┐
│  客户端   │      │  /api/user/login  │      │  微信/短信   │
│          │      │   (服务端)        │      │  服务商      │
└────┬─────┘      └────────┬─────────┘      └──────┬──────┘
     │                      │                       │
     │  ① POST /phone/verify_code                  │
     │  { phone: "+86-13800000000" }                │
     │─────────────────────>│                       │
     │                      │  ② 发送短信验证码      │
     │                      │──────────────────────│
     │                      │<─── 发送成功 ─────────│
     │<── 200 OK ───────────│                       │
     │  { needSecondVerify: false }                 │
     │                      │                       │
     │  ③ POST /phone                              │
     │  { phone: "+86-13800000000",                   │
     │    authMode: "CODE", code: "123456" }          │
     │─────────────────────>│                       │
     │                      │  ④ 验证 code → 通过   │
     │                      │  ⑤ 查询/创建用户       │
     │                      │  ⑥ 签发 token         │
     │<── 200 OK ───────────│                       │
     │  { token: "...",                             │
     │    userInfo: { ... } }                       │
     │                      │                       │
```

### 5.2 密码登录流程

```
┌──────────┐      ┌──────────────────┐
│  客户端   │      │  /api/user/login  │
└────┬─────┘      └────────┬─────────┘
     │                      │
     │  ① POST /phone                              │
     │  { phone: "+86-13800000000",                   │
     │    authMode: "PASSWORD",                       │
     │    password: "myPassword123" }                  │
     │─────────────────────>│                       │
     │                      │  ② 校验密码           │
     │                      │  ③ 查询用户           │
     │                      │  ④ 签发 token         │
     │<── 200 OK ───────────│                       │
     │  { token: "...",                             │
     │    userInfo: { ... } }                       │
```

### 5.3 Token 使用方式

登录成功返回的 `token` 需要在后续请求的 **HTTP Header** 中携带：

```
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

---

## 6. 附录：参数对象定义

### 6.1 AuthMode 枚举

| 值 | 说明 |
|----|------|
| `CODE` | 验证码模式（默认），校验短信/邮箱验证码 |
| `PASSWORD` | 密码模式，校验凭据密码 |

### 6.2 登录拦截与验证

| 注解 | 作用 |
|------|------|
| `@ApiController` | 统一响应封装、全局异常处理 |
| `@LoginVerify` | 需要用户已登录（携带有效 token），当前手机登录接口无需此注解 |

### 6.3 相关服务类

| 类 | 说明 |
|----|------|
| `UserLoginServiceBiz` | 登录/注册业务核心，处理所有验证、自动注册、token 签发 |
| `PhoneVerifyCodeSendServiceBiz` | 短信验证码发送，包含频率控制 |
| `VerifyCodeServiceBiz` | 验证码存储与校验（Redis 实现） |
| `LoginSigner` / `LoginVerifier` | RSA 签名 token 的签发与验证 |

### 6.4 手机号格式校验规则

手机号在服务端经过 `LoginSupport.isValidTelephone(phone)` 校验，规则如下：

| 条件 | 是否通过 | 说明 |
|------|----------|------|
| `+86-13800000000` | ✅ 通过 | 标准格式：`+` 开头 + 国家码 + `-` + 号码，且 libphonenumber 验证为有效号码 |
| `+8613800000000` | ❌ 不通过 | 缺少 `-` 分隔符 |
| `13800000000` | ❌ 不通过 | 不以 `+` 开头 |
| `+1-2125550123` | ✅ 通过 | 美国号码格式合法，但短信发送仅支持 `+86` |

> **注意**：登录接口（`/phone`）存储时原样保留 `phone` 值（含 `+` 前缀和 `-` 分隔符），不再做号码精简。  
> 短信发送（`/phone/verify_code`）则在 `PhoneVerifyCodeSendServiceBiz` 内部通过 `LoginSupport.normalizeCnSmsPhone()` 提取 11 位纯号码供阿里云 SDK 调用，但 `+86` 以外的国家码会抛出参数错误。
