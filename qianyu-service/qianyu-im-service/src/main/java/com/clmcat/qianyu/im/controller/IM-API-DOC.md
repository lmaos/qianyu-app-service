# IM 模块接口文档

> 基础路径: `/api/im`
> 当前未启用 `@LoginVerify`，`@Token` 返回 `userId=0`（sender 校验自动跳过）
> 生产环境启用后需在 Header 中携带 `Authorization: Bearer {JWT}`

---

## 1. 发送消息

`POST /api/im/send`

将消息转发到指定 IM 厂商的 REST API。服务器为纯转发代理，不修改 body 中任何字段，不存储消息。

### 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `channel` | string | 是 | IM 厂商标识：`tencent` / `easemob` / `rongcloud` / `nim` |
| `body` | object | 是 | 消息体（见下方 QYMessageBody） |

#### QYMessageBody

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `seqId` | string | 是 | 客户端排序 ID，格式 `{10位秒级时间戳}{4位自增}`，如 `"17481600000001"` |
| `msgId` | string | 是 | 客户端消息 ID，格式 `{sender}-{receiver}-{ts后8位}{4位自增}`，如 `"10001-10002-800000010001"` |
| `clientTime` | number | 是 | 客户端时间（毫秒时间戳） |
| `messageType` | string | 是 | 消息类型：`text`（文本）、`image`（图片）、`voice`（语音） |
| `content` | string | 是 | 消息内容。文本类型时为文字内容 |
| `sender` | string | 是 | 发送者用户 ID |
| `receiver` | string | 是 | 接收者用户 ID |
| `chatType` | number | 是 | 会话类型：`1`=私聊、`2`=群聊、`3`=系统通知 |

### 成功响应

```json
{
  "requestId": "uuid",
  "state": "OK",
  "message": "success",
  "content": "OK",
  "status": 200
}
```

> Controller 返回 `"OK"`，框架自动包装为标准响应格式。

### 错误场景

| 条件 | 错误信息 |
|------|---------|
| body 为 null | `消息体不能为空` |
| content 为空 | `消息内容不能为空` |
| sender 为空 | `发送者不能为空` |
| receiver 为空 | `接收者不能为空` |
| sender ≠ 当前用户（userId > 0 时） | `发送者与当前用户不一致` |
| channel 不支持 | `不支持的 IM 渠道: xxx` |

### curl 示例

```bash
# 发送文本消息（腾讯云，10001 → 10002）
curl -X POST http://localhost:8080/api/im/send \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "tencent",
    "body": {
      "seqId": "17481600000001",
      "msgId": "10001-10002-800000010001",
      "clientTime": 1748160000000,
      "messageType": "text",
      "content": "你好，这是一条测试消息",
      "sender": "10001",
      "receiver": "10002",
      "chatType": 1
    }
  }'
```

```bash
# 发送文本消息（腾讯云，10002 → 10001，反向测试）
curl -X POST http://localhost:8080/api/im/send \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "tencent",
    "body": {
      "seqId": "17481600000002",
      "msgId": "10002-10001-800000020001",
      "clientTime": 1748160001000,
      "messageType": "text",
      "content": "收到，这是回复消息",
      "sender": "10002",
      "receiver": "10001",
      "chatType": 1
    }
  }'
```

```bash
# 携带 JWT 的完整请求（生产环境）
curl -X POST http://localhost:8080/api/im/send \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "tencent",
    "body": {
      "seqId": "17481600000003",
      "msgId": "10001-10002-800000030001",
      "clientTime": 1748160002000,
      "messageType": "text",
      "content": "生产环境消息",
      "sender": "10001",
      "receiver": "10002",
      "chatType": 1
    }
  }'
```

```bash
# 错误示例：消息内容为空
curl -X POST http://localhost:8080/api/im/send \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "tencent",
    "body": {
      "seqId": "17481600000004",
      "msgId": "10001-10002-800000040001",
      "clientTime": 1748160003000,
      "messageType": "text",
      "content": "",
      "sender": "10001",
      "receiver": "10002",
      "chatType": 1
    }
  }'
# 返回错误: 消息内容不能为空
```

```bash
# 错误示例：不支持的 channel
curl -X POST http://localhost:8080/api/im/send \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "unknown_vendor",
    "body": {
      "seqId": "17481600000005",
      "msgId": "10001-10002-800000050001",
      "clientTime": 1748160004000,
      "messageType": "text",
      "content": "test",
      "sender": "10001",
      "receiver": "10002",
      "chatType": 1
    }
  }'
# 返回错误: 不支持的 IM 渠道: unknown_vendor
```

---

## 2. 获取 IM 登录凭证

`POST /api/im/login`

为指定用户生成 IM 登录凭证（Token/UserSig）。服务端先确保用户已在厂商平台注册，再生成凭证返回给客户端。客户端拿到 `imToken` 后调用对应厂商 SDK 的 login 方法。

### 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `channel` | string | 否 | IM 厂商标识。为空时使用配置的默认渠道（`qianyu.im.defaultChannel`） |

### 成功响应

```json
{
  "requestId": "uuid",
  "state": "OK",
  "message": "success",
  "content": {
    "imToken": "eJyrVgrx...",
    "channel": "tencent"
  },
  "status": 200
}
```

#### ImLoginResultDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `imToken` | string | IM 登录凭证。腾讯云为 UserSig（Base64 编码），环信/融云/网易云信为 Token 字符串 |
| `channel` | string | 实际使用的厂商标识 |

### 各厂商 Token 格式

| 厂商 | imToken 格式 | 有效期 | 生成方式 |
|------|-------------|--------|---------|
| 腾讯云 | UserSig（Base64 字符串） | 180 天 | 服务端用 SecretKey + HMAC-SHA256 生成 |
| 环信 | Bearer Token | 服务端决定 | 动态计算 SHA256（零网络开销） |
| 融云 | Token 字符串 | 永久 | POST `/user/getToken.json` 获取 |
| 网易云信 | Token 字符串 | 永久 | 服务端用 AppSecret 计算 |

### curl 示例

```bash
# 获取腾讯云 IM 登录凭证
curl -X POST http://localhost:8080/api/im/login \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "tencent"
  }'
```

```bash
# 不指定 channel，使用默认渠道
curl -X POST http://localhost:8080/api/im/login \
  -H "Content-Type: application/json" \
  -d '{}'
```

```bash
# 携带 JWT 获取凭证（生产环境，@Token 从 JWT 提取 userId）
curl -X POST http://localhost:8080/api/im/login \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "tencent"
  }'
```

---

## 3. 刷新 IM 登录凭证

`POST /api/im/refresh`

刷新用户的 IM 登录凭证。部分厂商（如环信、网易云信）Token 永久有效，刷新等同于重新生成。

### 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `channel` | string | 否 | IM 厂商标识。为空时使用配置的默认渠道 |

### 成功响应

```json
{
  "requestId": "uuid",
  "state": "OK",
  "message": "success",
  "content": {
    "imToken": "eJyrVgrx...(新 Token)",
    "channel": "tencent"
  },
  "status": 200
}
```

> 响应格式与 `/login` 完全一致。

### curl 示例

```bash
# 刷新腾讯云 IM 凭证
curl -X POST http://localhost:8080/api/im/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "tencent"
  }'
```

```bash
# 携带 JWT 刷新凭证（生产环境）
curl -X POST http://localhost:8080/api/im/refresh \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "tencent"
  }'
```

---

## 附录

### A. 枚举值速查

#### channel（厂商标识）

| 值 | 厂商 |
|----|------|
| `tencent` | 腾讯云 IM |
| `easemob` | 环信 IM |
| `rongcloud` | 融云 IM |
| `nim` | 网易云信 IM |

#### chatType（会话类型）

| 值 | 含义 |
|----|------|
| `1` | 私聊（SINGLE） |
| `2` | 群聊（GROUP） |
| `3` | 系统通知（SYSTEM） |

#### messageType（消息类型）

| 值 | 含义 |
|----|------|
| `text` | 文本消息 |
| `image` | 图片消息 |
| `voice` | 语音消息 |

### B. ID 生成规则

```
seqId  = {10位秒级时间戳}{4位自增数}
         例: 17481600000001

msgId  = {sender}-{receiver}-{时间戳后8位}{4位自增数}
         例: 10001-10002-800000010001
```

### C. 测试账号

当前腾讯云 IM 测试账号：

| userId | 用途 |
|--------|------|
| `10001` | 发送方 |
| `10002` | 接收方 |

### D. 快速验证流程

```bash
# 1. 获取登录凭证
curl -s -X POST http://localhost:8080/api/im/login \
  -H "Content-Type: application/json" \
  -d '{"channel":"tencent"}' | python -m json.tool

# 2. 发送消息 10001 → 10002
curl -s -X POST http://localhost:8080/api/im/send \
  -H "Content-Type: application/json" \
  -d '{
    "channel":"tencent",
    "body":{
      "seqId":"17481600000001",
      "msgId":"10001-10002-800000010001",
      "clientTime":1748160000000,
      "messageType":"text",
      "content":"hello",
      "sender":"10001",
      "receiver":"10002",
      "chatType":1
    }
  }' | python -m json.tool

# 3. 发送消息 10002 → 10001（双向验证）
curl -s -X POST http://localhost:8080/api/im/send \
  -H "Content-Type: application/json" \
  -d '{
    "channel":"tencent",
    "body":{
      "seqId":"17481600000002",
      "msgId":"10002-10001-800000020001",
      "clientTime":1748160001000,
      "messageType":"text",
      "content":"收到",
      "sender":"10002",
      "receiver":"10001",
      "chatType":1
    }
  }' | python -m json.tool

# 4. 刷新凭证
curl -s -X POST http://localhost:8080/api/im/refresh \
  -H "Content-Type: application/json" \
  -d '{"channel":"tencent"}' | python -m json.tool
```
