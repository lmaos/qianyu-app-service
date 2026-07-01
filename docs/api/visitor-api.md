# 访客记录 API

## 1. 概述

访客模块提供"谁看过我"（访客列表）和"我看过谁"（浏览历史）两个方向的数据查询，采用双表设计（`user_visitor` + `user_history`），支持雪花 ID 游标分页。

- **基础路径**：`/api/social/visitor`
- **认证**：所有接口需在 Header 中携带 `token`（JWT）
- **响应格式**：`{status, state, content, message}`

---

## 2. 数据模型

### 2.1 双表设计

| 表 | 唯一键 | 查询方向 | 分表键 |
|---|--------|---------|--------|
| `user_visitor` | `(visitee_id, visitor_id)` | 谁看过我 | `visitee_id` |
| `user_history` | `(visitor_id, visitee_id)` | 我看过谁 | `visitor_id` |

写入时事务双写（`INSERT ... ON DUPLICATE KEY UPDATE`），同一对 `(visitor, visitee)` 多次访问时 `visit_count` 递增、`is_new` 重置为 1、时间更新。

### 2.2 游标分页

- 游标字段：雪花 ID（`id`）
- 排序：`id DESC`
- 首页不传游标，后续页传入上一页最后一条记录的 `id`
- 通过 `hasMore` 判断是否有下一页

---

## 3. 接口列表

### 3.1 记录主页访问

```
POST /api/social/visitor/record
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | Header | 是 | 登录 Token |
| targetId | Body(Long) | 是 | 被访问的主页主人 ID |

请求示例：
```json
{"targetId": 5257117397155842}
```

> 不能访问自己的主页，会返回 `VISIT_SELF_NOT_ALLOWED` 错误。

---

### 3.2 查询访客列表（谁看过 ta）

```
GET /api/social/visitor/list?userId={userId}&nextId={nextId}&limit={limit}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 被查询用户 ID |
| nextId | Long | 否 | 游标（上一页最后一条雪花 ID） |
| limit | Integer | 否 | 分页大小，默认 20，最大 100 |

响应 `content`：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 被查询用户 ID |
| hasMore | Boolean | 是否有下一页 |
| nextId | Long | 下一页游标（hasMore=true 时有效） |
| userList | List | 访客用户列表 |

`userList` 元素：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 访客记录 ID |
| userId | Long | 访问者用户 ID |
| visitCount | Integer | 累计访问次数 |
| clientTime | Long | 最近访问时间戳 |
| nickname | String | 用户昵称 |
| avatar | String | 用户头像 URL |

---

### 3.3 查询我的访客列表

```
GET /api/social/visitor/self/list?nextId={nextId}&limit={limit}
```

同上，但 `userId` 自动取当前登录用户。**调用后自动清除新访客标记。**

---

### 3.4 查询浏览历史（ta 看过谁）

```
GET /api/social/visitor/history/list?userId={userId}&nextId={nextId}&limit={limit}
```

响应结构与访客列表相同。

---

### 3.5 查询我的浏览历史

```
GET /api/social/visitor/history/self/list?nextId={nextId}&limit={limit}
```

---

### 3.6 删除访客记录

```
POST /api/social/visitor/delete
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | Header | 是 | 登录 Token |
| targetId | Body(Long) | 是 | 要删除的访问者 ID |

> 仅删除 `user_visitor` 表中的记录，不删除对方的浏览历史。

---

### 3.7 删除浏览历史

```
POST /api/social/visitor/history/delete
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | Header | 是 | 登录 Token |
| targetId | Body(Long) | 是 | 要删除的被访问者 ID |

> 仅删除 `user_history` 表中的记录，不删除对方的访客记录。

---

### 3.8 查询访客数量

```
GET /api/social/visitor/count?userId={userId}
```

响应 `content`：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户 ID |
| visitorCount | Long | 新访客数（is_new=1 的记录数） |

---

### 3.9 查询我的访客数量

```
GET /api/social/visitor/count/self
```

同上，`userId` 自动取当前登录用户。

---

## 4. 个人中心集成

- **快捷入口**：`"新访客"` 已激活，在个人中心 `shortcuts` 中返回，linkUrl 指向 `page://user/visitor-list`
- **访客数量**：`userStats.visitorCount` 调用 `VisitorApi.getVisitorCount()` 获取（已替换原来的 `return 0` 桩实现）
- **浏览历史 Tab**：`tab=history` 暂未接入 `user_history` 表数据，当前返回空列表（TODO）

---

## 5. 数据库表

SQL 文件：`qianyu-service/qianyu-social-service/sql/user_visitor.sql`

- `user_visitor` — 访客记录表（谁看过我）
- `user_history` — 浏览历史表（我看过谁）
