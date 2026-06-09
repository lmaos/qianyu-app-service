# 微信登录 API 文档

## 目录

1. [通用说明](#1-通用说明)
2. [微信社交登录](#2-微信社交登录)
3. [完整调用流程](#3-完整调用流程)
4. [公共错误码](#4-公共错误码)
5. [附录](#5-附录)

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
| content | Object | 业务数据（登录成功返回 `LoginResultVo`，失败时为 `null`） |
| message | String | 附加说明信息 |

详细约定见 [docs/api-struct.md](../api-struct.md)。

### 1.2 HTTP 状态码含义

| HTTP 状态码 | 含义 | 场景 |
|-------------|------|------|
| 200 | 成功 | 请求正常处理 |
| 400 | 参数错误 (P_ERROR) | 必填参数缺失 |
| 401 | 登录失败 (U_ERROR) | 微信授权失败、code 无效、账户异常 |
| 422 | 业务错误 (R_ERROR) | 不支持的平台类型 |
| 503 | 下游服务异常 (F_ERROR) | 微信 API 不可用 |

### 1.3 接口前缀

```
POST /api/user/login
```

所有接口均使用 `POST` 方法，请求体以 `application/json` 格式提交。

---

## 2. 微信社交登录

使用微信授权码（`code`）完成登录。如果微信 openid 尚未注册，会自动完成注册并返回登录结果。

### 2.1 接口定义

```
POST /api/user/login/social
```

### 2.2 请求参数

**SocialLoginDto**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| platform | String | 是 | 第三方平台类型，微信登录传 `WECHAT` |
| code | String | 是 | 微信授权 code（前端通过微信 OAuth 授权获取，一次有效） |

```json
{
  "platform": "WECHAT",
  "code": "033a8k000w6H2e1VQr3X00xfPw3a8k0R"
}
```

**AuthPlatform 枚举**

| 值 | 说明 |
|----|------|
| `WECHAT` | 微信登录 ✅ 当前已支持 |
| `QQ` | QQ 登录（预留，暂未实现） |
| `GOOGLE` | Google 登录（预留，暂未实现） |
| `FACEBOOK` | Facebook 登录（预留，暂未实现） |

> 传入非 `WECHAT` 的 `platform` 值将返回 `400020` 不支持的操作错误。

### 2.3 响应参数

**LoginResultVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | 登录签名 token，需携带在后续请求 Header 中 |
| userInfo | UserInfoVo | 用户基本信息 |

**UserInfoVo 字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| userNo | String | 用户外显编号 |
| userId | Long | 用户内部 ID |
| nickname | String | 昵称（首次登录来自微信昵称，之后可修改） |
| avatar | String | 头像 URL（首次登录来自微信头像） |
| bio | String | 个人简介 |
| gender | Integer | 性别：`0`-未知，`1`-女性，`2`-男性 |
| birthday | LocalDate | 生日 |
| age | Integer | 年龄 |
| phone | String | 手机号（微信登录可能为空） |
| phoneVerifiedTime | Long | 手机号认证通过时间戳 |
| email | String | 邮箱 |
| country | String | 国家/地区代码，微信登录默认 `CN` |
| province | String | 省份 |
| city | String | 城市 |
| lastLoginTime | Long | 最后登录时间戳 |
| status | Integer | 账号状态 |
| freezeEndTime | Long | 冻结结束时间戳 |
| createTime | Long | 创建时间戳 |
| updateTime | Long | 更新时间戳 |

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
      "nickname": "微信用户昵称",
      "avatar": "https://thirdwx.qlogo.cn/...",
      "bio": "",
      "gender": 1,
      "birthday": null,
      "age": 0,
      "phone": null,
      "phoneVerifiedTime": null,
      "email": null,
      "country": "CN",
      "province": "Guangdong",
      "city": "Shenzhen",
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

### 2.4 业务说明

| 要点 | 说明 |
|------|------|
| 自动注册 | 微信 openid 不存在时自动创建用户：`user_auth.identifier` 存储 openid，`user_info.nickname` 和 `avatar` 优先取微信昵称/头像 |
| 自动登录 | 微信 openid 已存在时直接登录，返回已绑定用户的信息 |
| code 一次性 | 微信授权 code 仅能使用一次，重复使用会触发微信 API 返回 `errcode=40029` |
| 用户信息降级 | 获取微信用户信息（昵称/头像）失败时，不影响登录流程，仅使用 openid 完成注册/登录，昵称默认使用 Base36 编码的 userId |
| token 签发 | 登录成功后签发 RSA 签名 token（RS256），后续请求需在 Header 携带 |

### 2.5 微信 OAuth 授权说明

前端需要先引导用户跳转微信授权页面，获取 `code` 后调用此接口。

**微信授权 URL 格式（需在微信客户端或网页中打开）：**

```
https://open.weixin.qq.com/connect/oauth2/authorize
  ?appid=APPID
  &redirect_uri=REDIRECT_URI
  &response_type=code
  &scope=snsapi_userinfo
  &state=STATE#wechat_redirect
```

授权成功后，微信回调 `redirect_uri` 并携带 `code` 参数，前端获取 `code` 后调用 `/api/user/login/social` 完成登录。

### 2.6 常见错误

| 错误码 | HTTP 状态 | 说明 |
|--------|-----------|------|
| 300004 | 400 | platform 为空或不支持 |
| 100002 | 401 | 微信 API 返回错误，如 code 无效（`errcode=40029`）、access_token 过期等 |
| 200005 | 401 | 账户被冻结，不允许登录 |
| 400003 | 422 | 用户数据不存在（罕见） |
| 400020 | 422 | 不支持的社交平台（platform 非 WECHAT） |
| 500003 | 503 | 微信 API 调用异常（网络超时、服务暂不可用） |

---

## 3. 完整调用流程

### 3.1 微信登录流程

```
┌──────────┐     ┌──────────────────┐     ┌─────────────┐
│  客户端   │     │  /api/user/login  │     │  微信 API    │
│  (前端)   │     │   (服务端)        │     │             │
└────┬─────┘     └────────┬─────────┘     └──────┬──────┘
     │                     │                      │
     │  ① 引导用户跳转微信授权                    │
     │  ─────────────────────────────────────────>
     │                     │                      │
     │  ② 用户确认授权，回调返回 code             │
     │  <─────────────────────────────────────────
     │                     │                      │
     │  ③ POST /social                            │
     │  { platform: "WECHAT",                     │
     │    code: "033a8k0..." }                    │
     │ ────────────────>│                         │
     │                   │  ④ 用 code 换取        │
     │                   │     access_token       │
     │                   │     + openid           │
     │                   │ ─────────────────────>│
     │                   │  <─────────────────────│
     │                   │  ⑤ 用 access_token     │
     │                   │     + openid 获取       │
     │                   │     用户信息            │
     │                   │ ─────────────────────>│
     │                   │  <─────────────────────│
     │                   │  ⑥ 查询/创建用户       │
     │                   │  ⑦ 签发 token          │
     │  <────────────────│                        │
     │  { token: "...",                           │
     │    userInfo: { ... } }                     │
```

### 3.2 服务端处理流程（详细）

```
socialLogin(dto)
  │
  ├─ platform == WECHAT ?
  │     ↓
  │  weChatLoginServiceBiz.getWechatUserAuthDto(code)
  │     │
  │     ├─ 1. wechatApiClient.getAccessToken(code)
  │     │       GET https://api.weixin.qq.com/sns/oauth2/access_token
  │     │       ?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
  │     │       → 返回 { access_token, openid, ... }
  │     │
  │     ├─ 2. wechatApiClient.getUserInfo(accessToken, openid)
  │     │       GET https://api.weixin.qq.com/sns/userinfo
  │     │       ?access_token=TOKEN&openid=OPENID
  │     │       → 返回 { nickname, headimgurl, sex, country, ... }
  │     │       ⚠ 失败仅记日志，不影响流程
  │     │
  │     └─ 3. 封装 UserAuthDto
  │            { identifier=openid, identityType="WECHAT",
  │              socialNickname=nickname, socialAvatar=headimgurl,
  │              country="CN" }
  │
  ├─ loginOrRegister(authDto)
  │     ├─ openid 存在 → 直接登录
  │     └─ openid 不存在 → 创建新用户
  │           user_auth: { userId, identityType="WECHAT", identifier=openid }
  │           user_info: { nickname(微信昵称/Base36编码), avatar, ... }
  │
  └─ toLoginResultDto() → toLoginResultVo()
        → { token, userInfo }
```

### 3.3 Token 使用方式

登录成功返回的 `token` 需要在后续请求的 **HTTP Header** 中携带：

```
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

---

## 4. 公共错误码

以下错误码在本接口中通用：

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 0 | 200 | OK | 处理成功 |
| 300001 | 400 | P_ERROR | 参数错误（通用） |
| 300004 | 400 | P_VALUE_ERROR | 参数值校验不通过（platform 为空/不支持） |
| 100002 | 401 | AUTH_LOGIN_FAIL | 微信登录失败（微信 API 返回业务错误） |
| 200005 | 401 | U_FREEZE | 账户被冻结 |
| 400001 | 422 | R_ERROR | 业务异常（通用） |
| 400003 | 422 | R_NOEXIST_DATA | 数据不存在 |
| 400020 | 422 | R_NOT_SUPPORTED | 不支持的平台 |
| 500 | 500 | SYSTEM_ERROR | 系统内部错误 |
| 500003 | 503 | F_SERVICE_UNAVAILABLE | 微信服务暂不可用 |

---

## 5. 附录

### 5.1 微信 API 返回结果说明

**access_token 接口响应**

```json
{
  "access_token": "ACCESS_TOKEN",
  "expires_in": 7200,
  "refresh_token": "REFRESH_TOKEN",
  "openid": "OPENID",
  "scope": "snsapi_userinfo"
}
```

| 字段 | 说明 |
|------|------|
| access_token | 接口调用凭证，有效期 2 小时 |
| expires_in | 凭证有效时间（秒） |
| refresh_token | 刷新凭证（用于续期 access_token） |
| openid | 用户在当前 AppID 下的唯一标识 |
| scope | 用户授权的作用域 |

**userinfo 接口响应（部分字段）**

```json
{
  "openid": "OPENID",
  "nickname": "NICKNAME",
  "sex": 1,
  "province": "Guangdong",
  "city": "Shenzhen",
  "country": "CN",
  "headimgurl": "https://thirdwx.qlogo.cn/...",
  "unionid": "UNIONID"
}
```

| 字段 | 说明 |
|------|------|
| nickname | 用户昵称 |
| sex | 性别：1-男，2-女，0-未知 |
| headimgurl | 用户头像 URL |
| unionid | 用户全局标识（开放平台绑定后返回） |

**错误响应**

```json
{
  "errcode": 40029,
  "errmsg": "invalid code"
}
```

| errcode | 说明 |
|---------|------|
| 40029 | code 无效（已过期、已使用、或 AppID 不匹配） |
| 40163 | code 已被使用 |
| -1 | 系统繁忙 |

### 5.2 服务端配置参数

配置文件：`application-login.yml`

```yaml
qianyu:
  login:
    wechat:
      app-id:      # 微信开放平台 / 公众号的 AppID
      app-secret:  # 微信开放平台 / 公众号的 AppSecret
```

> `tokenUrl` 和 `userInfoUrl` 有默认值，一般无需配置：
> - tokenUrl: `https://api.weixin.qq.com/sns/oauth2/access_token`
> - userInfoUrl: `https://api.weixin.qq.com/sns/userinfo`

### 5.3 相关服务类

| 类 | 说明 |
|----|------|
| `UserLoginServiceBiz` | 登录/注册业务核心，社交登录入口 |
| `WeChatLoginServiceBiz` | 微信登录业务：code → UserAuthDto |
| `WechatApiClient` | 微信 API HTTP 客户端（access_token / userinfo） |
| `WechatApiResult` | 微信 API 响应模型映射 |
| `WechatLoginProperties` | 微信登录配置（appId、appSecret） |
| `LoginSigner` / `LoginVerifier` | RSA 签名 token 的签发与验证 |

### 5.4 与手机号登录的区别

| 对比项 | 微信登录 | 手机号登录 |
|--------|----------|------------|
| 用户标识 | openid（微信分配） | 手机号（`+86-xxxxxxxxxxx`） |
| 身份类型 | `WECHAT` | `phone` |
| 首次昵称 | 来自微信昵称（有则取，无则回退） | Base36 编码 userId |
| 首次头像 | 来自微信头像（有则取，无则为空） | 空 |
| 国家码 | 固定为 `CN`（微信平台决定） | `CN` |
