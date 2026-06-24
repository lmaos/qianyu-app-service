# 关注关系 API 文档

> 面向前端接入 / AI 参考。覆盖关注、取关、关系查询、关注列表、粉丝列表和数量统计相关 HTTP 接口。

## 目录

1. [通用说明](#1-通用说明)
2. [关注用户](#2-关注用户)
3. [取消关注](#3-取消关注)
4. [查询关注关系](#4-查询关注关系)
5. [查询关注列表](#5-查询关注列表)
6. [查询我的关注列表](#6-查询我的关注列表)
7. [查询粉丝列表](#7-查询粉丝列表)
8. [查询我的粉丝列表](#8-查询我的粉丝列表)
9. [查询关注和粉丝数量](#9-查询关注和粉丝数量)
10. [查询我的关注和粉丝数量](#10-查询我的关注和粉丝数量)
11. [公共错误码](#11-公共错误码)
12. [附录：对象定义](#12-附录对象定义)

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
/api/social/follow
```

所有接口按语义使用 `GET` 或 `POST`。Token 通过 Header `token` 传递。

### 1.4 数据存储模型

关注关系使用**双表冗余**设计（`follow` 表 + `follower` 表）：

| 表 | 查询场景 | 索引 |
|------|------|------|
| `follow` | 查"我关注了谁"（关注列表） | `follower_id` + `id` 倒序 |
| `follower` | 查"谁关注了我"（粉丝列表） | `followee_id` + `id` 倒序 |

关注时两表**同时写入**（同一事务），取关时**同时删除**。互关好友状态（`is_friend`）在两表中冗余存储，避免 JOIN 查询。

### 1.5 游标分页说明

列表接口使用**基于关系记录 ID 的倒序游标分页**（非 offset），避免深分页性能问题：

| 参数 | 说明 |
|------|------|
| nextId | 上一页最后一条**关系记录的雪花 ID**（非 userId），首页不传 |
| limit | 每页条数，默认 20，最大 100 |
| hasMore | 响应 `true`/`false`，`true` 时取末尾 `id` 作为下一页 `nextId` |

翻页流程：
1. 首页不传 `nextId`（或传 `0`），服务端按 `id < Long.MAX_VALUE` 倒序查 `limit+1` 条
2. 若返回 `limit+1` 条则 `hasMore=true`，取末尾元素的 `id` 作为 `nextId`
3. 下一页传 `nextId=上一页末尾id`，重复步骤 2

---

## 2. 关注用户

当前登录用户关注目标用户。

### 2.1 接口定义

```
POST /api/social/follow/follow
```

### 2.2 请求参数

**FollowTargetDto**（JSON Body / form）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetId | Long | 是 | 要关注的目标用户 ID |

```json
{
  "targetId": 2495058814603265
}
```

### 2.3 响应参数

`content` 为 `boolean`：`true` 表示操作成功，`false` 表示失败。

### 2.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": true,
  "message": "OK"
}
```

### 2.5 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | targetId 为空或 <= 0 |
| 100003 | 403 | token 失效或未传 |
| 400001 | 422 | 不能关注自己 / 已关注 / 目标用户不存在 |

---

## 3. 取消关注

当前登录用户取消关注目标用户。

### 3.1 接口定义

```
POST /api/social/follow/cancel
```

### 3.2 请求参数

**FollowTargetDto**（JSON Body / form）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetId | Long | 是 | 要取消关注的目标用户 ID |

```json
{
  "targetId": 2495058814603265
}
```

### 3.3 响应参数

`content` 为 `boolean`：`true` 表示操作成功，`false` 表示失败。

### 3.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": true,
  "message": "OK"
}
```

### 3.5 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | targetId 为空或 <= 0 |
| 100003 | 403 | token 失效或未传 |
| 400001 | 422 | 不能取关自己 / 未关注 / 目标用户不存在 |

---

## 4. 查询关注关系

查询当前登录用户与目标用户之间的关注关系。

### 4.1 接口定义

```
GET /api/social/follow/relation?targetId={userId}
```

### 4.2 请求参数

**FollowTargetDto**（query 参数）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetId | Long | 是 | 目标用户 ID |

```json
{
  "targetId": 2495058814603265
}
```

### 4.3 响应参数

`content` 为 [FollowRelationVo](#121-followrelationvo) 对象。

### 4.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userId": 2495058814603264,
    "targetUserId": 2495058814603265,
    "follow": true,
    "follower": false,
    "friend": false
  },
  "message": "OK"
}
```

| 字段 | 说明 |
|------|------|
| follow=true | 当前用户已关注目标用户 |
| follower=true | 目标用户已关注当前用户 |
| friend=true | 互相关注（双方互关） |

### 4.5 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | targetId 为空或 <= 0 |
| 100003 | 403 | token 失效或未传 |

---

## 5. 查询关注列表

查询指定用户的关注列表（ta 关注了谁）。

### 5.1 接口定义

```
GET /api/social/follow/followee/list
```

### 5.2 请求参数

**FollowListQueryDto**（query 参数）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 被查询用户 ID |
| nextId | Long | 否 | 倒序分页游标，首页不传 |
| limit | Integer | 否 | 分页大小，默认 20，最大 100 |

```
GET /api/social/follow/followee/list?userId=2495058814603264&nextId=0&limit=20
```

### 5.3 响应参数

`content` 为 [FollowPageVo](#122-followpagevo) 对象。

### 5.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userId": 2495058814603264,
    "nextId": 10,
    "hasMore": true,
    "followList": [
      {
        "id": 100,
        "userId": 2495058814603265,
        "isFriend": 1,
        "clientTime": 1719123456000,
        "nickname": "用户A",
        "avatar": "https://cdn.example.com/a.jpg"
      },
      {
        "id": 99,
        "userId": 2495058814603266,
        "isFriend": 0,
        "clientTime": 1719120000000,
        "nickname": "用户B",
        "avatar": "https://cdn.example.com/b.jpg"
      }
    ]
  },
  "message": "OK"
}
```

> **说明**：`nickname` 和 `avatar` 通过 UserApi 批量查询用户信息填充，无数据时为 `null`。

### 5.5 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | userId 为空或 <= 0 |
| 100003 | 403 | token 失效或未传 |

---

## 6. 查询我的关注列表

查询当前登录用户自己的关注列表。

### 6.1 接口定义

```
GET /api/social/follow/followee/self/list
```

### 6.2 请求参数

**FollowSelfListQueryDto**（query 参数）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nextId | Long | 否 | 倒序分页游标，首页不传 |
| limit | Integer | 否 | 分页大小，默认 20，最大 100 |

```
GET /api/social/follow/followee/self/list?nextId=0&limit=20
```

### 6.3 响应参数

`content` 为 [FollowPageVo](#122-followpagevo) 对象。

### 6.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userId": 2495058814603264,
    "nextId": 10,
    "hasMore": true,
    "followList": [
      {
        "id": 100,
        "userId": 2495058814603265,
        "isFriend": 1,
        "clientTime": 1719123456000,
        "nickname": "用户A",
        "avatar": "https://cdn.example.com/a.jpg"
      }
    ]
  },
  "message": "OK"
}
```

---

## 7. 查询粉丝列表

查询指定用户的粉丝列表（谁关注了 ta）。

### 7.1 接口定义

```
GET /api/social/follow/follower/list
```

### 7.2 请求参数

**FollowListQueryDto**（query 参数）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 被查询用户 ID |
| nextId | Long | 否 | 倒序分页游标，首页不传 |
| limit | Integer | 否 | 分页大小，默认 20，最大 100 |

```
GET /api/social/follow/follower/list?userId=2495058814603264&nextId=0&limit=20
```

### 7.3 响应参数

`content` 为 [FollowPageVo](#122-followpagevo) 对象。

### 7.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userId": 2495058814603264,
    "nextId": 10,
    "hasMore": true,
    "followList": [
      {
        "id": 200,
        "userId": 2495058814603270,
        "isFriend": 0,
        "clientTime": 1719123456000,
        "nickname": "粉丝C",
        "avatar": "https://cdn.example.com/c.jpg"
      }
    ]
  },
  "message": "OK"
}
```

---

## 8. 查询我的粉丝列表

查询当前登录用户自己的粉丝列表。

### 8.1 接口定义

```
GET /api/social/follow/follower/self/list
```

### 8.2 请求参数

**FollowSelfListQueryDto**（query 参数）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nextId | Long | 否 | 倒序分页游标，首页不传 |
| limit | Integer | 否 | 分页大小，默认 20，最大 100 |

```
GET /api/social/follow/follower/self/list?nextId=0&limit=20
```

### 8.3 响应参数

`content` 为 [FollowPageVo](#122-followpagevo) 对象。

### 8.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userId": 2495058814603264,
    "nextId": 10,
    "hasMore": true,
    "followList": [
      {
        "id": 200,
        "userId": 2495058814603270,
        "isFriend": 0,
        "clientTime": 1719123456000,
        "nickname": "粉丝C",
        "avatar": "https://cdn.example.com/c.jpg"
      }
    ]
  },
  "message": "OK"
}
```

---

## 9. 查询关注和粉丝数量

查询指定用户的关注数和粉丝数。

### 9.1 接口定义

```
GET /api/social/follow/count?userId={userId}
```

### 9.2 请求参数

**FollowUserQueryDto**（query 参数）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 被查询用户 ID |

```json
{
  "userId": 2495058814603264
}
```

### 9.3 响应参数

`content` 为 [FollowCountVo](#123-followcountvo) 对象。

### 9.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userId": 2495058814603264,
    "followCount": 128,
    "followerCount": 256
  },
  "message": "OK"
}
```

### 9.5 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | userId 为空或 <= 0 |
| 100003 | 403 | token 失效或未传 |

---

## 10. 查询我的关注和粉丝数量

查询当前登录用户自己的关注数和粉丝数。

### 10.1 接口定义

```
GET /api/social/follow/count/self
```

### 10.2 请求参数

无（userId 由 Header `token` 自动解析）。

### 10.3 响应参数

`content` 为 [FollowCountVo](#123-followcountvo) 对象。

### 10.4 响应示例

```json
{
  "status": 0,
  "state": "OK",
  "content": {
    "userId": 2495058814603264,
    "followCount": 128,
    "followerCount": 256
  },
  "message": "OK"
}
```

---

## 11. 公共错误码

以下错误码在所有关注关系接口中通用：

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 0 | 200 | OK | 成功 |
| 300004 | 400 | P_VALUE_ERROR | 参数值校验不通过（userId/targetId <= 0、必填参数缺失等） |
| 100001 | 401 | AUTH_NOT_LOGIN | 未登录 |
| 100003 | 403 | AUTH_TOKEN_INVALID | token 失效或未传 |
| 400001 | 422 | R_ERROR | 业务异常（通用，如不能关注自己、重复关注等） |
| 400002 | 422 | R_OPERATION_FAIL | 操作失败 |
| 500 | 500 | SYSTEM_ERROR | 系统内部错误 |

---

## 12. 附录：对象定义

### 12.1 FollowRelationVo

关注关系返回对象，用于关系查询接口。

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 当前登录用户 ID |
| targetUserId | Long | 目标用户 ID |
| follow | boolean | 当前用户是否关注了目标用户 |
| follower | boolean | 目标用户是否关注了当前用户 |
| friend | boolean | 是否互相关注（`follow && follower`） |

### 12.2 FollowPageVo

关注/粉丝列表分页返回对象。

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 被查询用户 ID |
| nextId | Long | 下一页游标（末条记录的雪花 ID），`0` 表示无下一页 |
| hasMore | boolean | 是否还有更多数据 |
| followList | List\<[FollowUserVo](#124-followuservo)\> | 关注/粉丝用户列表（已含昵称、头像） |

### 12.3 FollowCountVo

关注和粉丝数量返回对象。

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 被查询用户 ID |
| followCount | Long | 关注数（ta 关注了多少人） |
| followerCount | Long | 粉丝数（多少人关注了 ta） |

### 12.4 FollowUserVo

列表中的单个关注用户信息。基础字段来自 `follow`/`follower` 表，`nickname`/`avatar` 通过 `UserApi` 批量查询填充。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 关注关系记录 ID（雪花 ID），**用于游标分页的 `nextId`** |
| userId | Long | 用户 ID（关注列表中是 `followeeId`，粉丝列表中是 `followerId`） |
| isFriend | Integer | 是否互关好友：`0` 否，`1` 是 |
| clientTime | Long | 关注/被关注时间戳（Unix 毫秒） |
| nickname | String | 用户昵称（批量查询填充，无数据时为 `null`） |
| avatar | String | 用户头像 URL（批量查询填充，无数据时为 `null`） |

### 12.5 完整调用流程

```bash
# ===== 1. 关注用户 =====
curl -X POST http://localhost:8080/api/social/follow/follow \
  -H "Content-Type: application/json" \
  -H "token: <token>" \
  -d '{"targetId": 2495058814603265}'

# ===== 2. 查询关注关系 =====
curl -X GET "http://localhost:8080/api/social/follow/relation?targetId=2495058814603265" \
  -H "token: <token>"

# ===== 3. 查自己的关注列表 =====
curl -X GET "http://localhost:8080/api/social/follow/followee/self/list?limit=20" \
  -H "token: <token>"

# ===== 4. 查自己的粉丝列表 =====
curl -X GET "http://localhost:8080/api/social/follow/follower/self/list?limit=20" \
  -H "token: <token>"

# ===== 5. 查自己的关注和粉丝数量 =====
curl -X GET http://localhost:8080/api/social/follow/count/self \
  -H "token: <token>"

# ===== 6. 取消关注 =====
curl -X POST http://localhost:8080/api/social/follow/cancel \
  -H "Content-Type: application/json" \
  -H "token: <token>" \
  -d '{"targetId": 2495058814603265}'
```

### 12.6 服务模块

| 模块 | 说明 |
|------|------|
| `qianyu-social-service` | 社交服务实现（FollowController + FollowViewServiceBiz + FollowServiceBiz） |
| `qianyu-social-api` | 社交 Dubbo RPC 接口（FollowApi，供其他微服务调用） |

### 12.7 注解说明

| 注解 | 作用 |
|------|------|
| `@ApiController` | 统一响应封装（将返回值包入 `content`） + 全局异常处理 |
| `@LoginVerify` | 当前类所有接口需要登录态（Header 携带有效 token） |
| `@Token` | 自动从 token 中解析 userId 注入方法参数 |
| `@Params` | 声明参数来源（query/form/body 自动绑定） |
