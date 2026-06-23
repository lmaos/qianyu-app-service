# 社交 Feed 与互动 API 文档

## 目录

1. [通用说明](#1-通用说明)
2. [推荐 Feed 接口](#2-推荐-feed-接口)
3. [Feed 卡片摘要接口](#3-feed-卡片摘要接口)
4. [评论接口](#4-评论接口)
5. [点赞接口](#5-点赞接口)
6. [动态详情接口](#6-动态详情接口)
7. [公共错误码](#7-公共错误码)
8. [附录：对象定义](#8-附录对象定义)

---

## 1. 通用说明

### 1.1 认证

所有接口需要登录态，在 **HTTP Header** 中携带 token：

```
token: eyJhbGciOiJSUzI1NiJ9...
```

Token 获取方式：`POST /api/user/login/phone`，见 [login-phone-api.md](login-phone-api.md)。

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
| content | Object | 业务数据 |
| message | String | 附加说明信息 |

### 1.3 HTTP 状态码含义

| HTTP 状态码 | 含义 | 场景 |
|-------------|------|------|
| 200 | 成功 | 请求正常处理 |
| 400 | 参数错误 (P_ERROR) | 必填参数缺失、格式错误 |
| 401 | 登录失败 (U_ERROR) | token 失效 |
| 403 | 权限错误 (A_ERROR) | token 失效、无权限 |
| 422 | 业务错误 (R_ERROR) | 数据不存在、操作失败等业务逻辑 |
| 429 | 限流 (L_ERROR) | 请求过于频繁被限制 |
| 500 | 系统错误 (S_ERROR) | 服务端未预期异常 |

### 1.4 接口前缀

| 分组 | 前缀 |
|------|------|
| 推荐 Feed | `GET /api/social/feed` |
| 评论 | `/api/social/comment` |
| 点赞 | `/api/social/like` |
| 动态详情 | `/api/social/moment` |

详细约定见 [docs/api-struct.md](../api-struct.md)。

---

## 2. 推荐 Feed 接口

首页推荐流，使用 momentId 倒序游标分页。响应中的 `hasLike` 标识当前用户是否已点赞该动态。

### 2.1 推荐 Feed（为你推荐）

按 momentId 倒序返回最新动态（当前为简单时间倒序，TODO 后续接入推荐引擎）。

```
GET /api/social/feed/recommend
```

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| cursor | Long | 否 | `0` | 游标 momentId（上一页最后一条的 momentId），首次传 `0` |
| limit | int | 否 | `20` | 分页大小，范围 1-100 |

#### 响应参数

**FeedPageVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| nextCursor | Long | 下一页游标，传 0 表示没有更多数据 |
| hasMore | Boolean | 是否还有更多数据 |
| datas | List\<MomentVo\> | 动态列表（见 MomentVo 定义） |

#### 响应示例

```json
{
  "requestId": "322655903800561666",
  "status": 0,
  "state": "OK",
  "content": {
    "nextCursor": 5257117397155840,
    "hasMore": true,
    "datas": [
      {
        "momentId": 5257117397155845,
        "authorId": 100001,
        "content": {
          "type": "video",
          "text": {
            "text": "今天的日落 🌇",
            "atIds": []
          },
          "video": {
            "videoId": "v001",
            "videoUrl": "https://cdn.example.com/video.mp4",
            "coverUrl": "https://cdn.example.com/cover.jpg",
            "width": 1920,
            "height": 1080,
            "duration": 15
          }
        },
        "latitude": 22.5431,
        "longitude": 114.0579,
        "country": "CN",
        "likes": 42,
        "comments": 7,
        "hasLike": false,
        "status": 0,
        "createTime": 1718000000000
      }
    ]
  },
  "message": "OK"
}
```

---

### 2.2 关注 Feed（关注的人）

查询当前用户关注的用户发布的动态列表。

```
GET /api/social/feed/following
```

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| cursor | Long | 否 | `0` | 游标 momentId（上一页最后一条的 momentId），首次传 `0` |
| limit | int | 否 | `20` | 分页大小，范围 1-100 |

#### 响应参数

同 `GET /api/social/feed/recommend`，返回 **FeedPageVo**。

#### 响应示例

```json
{
  "requestId": "322655903800561667",
  "status": 0,
  "state": "OK",
  "content": {
    "nextCursor": 5257117397155830,
    "hasMore": false,
    "datas": [
      {
        "momentId": 5257117397155835,
        "authorId": 100002,
        "content": {
          "type": "image",
          "text": {
            "text": "今日午餐 🍜",
            "atIds": []
          },
          "image": [
            {
              "imageId": "img001",
              "imageUrl": "https://cdn.example.com/lunch.jpg",
              "width": 1080,
              "height": 1080
            }
          ]
        },
        "latitude": 0,
        "longitude": 0,
        "country": "CN",
        "likes": 18,
        "comments": 3,
        "hasLike": true,
        "status": 0,
        "createTime": 1717990000000
      }
    ]
  },
  "message": "OK"
}
```

### 2.3 游标分页说明

- `cursor=0` 表示从头开始，查询最新一页
- 响应中的 `nextCursor` 作为下一页的 `cursor` 传入
- `hasMore=false` 且 `nextCursor=0` 表示已到最后一页
- 每页最多 100 条，默认 20 条

### 2.4 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 100003 | 403 | token 失效或未传 |

---

## 3. Feed 卡片摘要接口

轻量版 Feed 接口，不返回完整的 content 嵌套结构，而是预提取封面图、标题、类型、计数等 UI 直接需要的字段。适合首页信息流、双列瀑布流等卡片场景。

### 3.1 推荐 Feed 卡片摘要

```
GET /api/social/feed/recommend/cards
```

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| cursor | Long | 否 | `0` | 游标 momentId，首次传 `0` |
| limit | int | 否 | `20` | 分页大小，范围 1-100 |

#### 响应参数

**FeedCardPageVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| nextCursor | Long | 下一页游标，0 表示无更多 |
| hasMore | Boolean | 是否还有更多数据 |
| datas | List\<FeedCardVo\> | 卡片列表 |

**FeedCardVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| momentId | Long | 作品 ID |
| authorId | Long | 作者 ID |
| nickname | String | 作者昵称 |
| avatar | String | 作者头像 URL |
| coverUrl | String | 封面图 URL（视频取 coverUrl，图文取第一张图） |
| title | String | 标题/描述文本 |
| type | String | 内容类型：`video` / `image` / `text` |
| likeCount | Long | 点赞数 |
| commentCount | Long | 评论数 |
| viewCount | Long | 观看数（当前返回 0，TODO 后续接入播放统计） |
| hasLike | boolean | 当前用户是否已点赞 |

#### 响应示例

```json
{
  "requestId": "322655903800561673",
  "status": 0,
  "state": "OK",
  "content": {
    "nextCursor": 5257117397155840,
    "hasMore": true,
    "datas": [
      {
        "momentId": 5257117397155845,
        "authorId": 100001,
        "nickname": "小明",
        "avatar": "https://cdn.example.com/avatar/100001.jpg",
        "coverUrl": "https://cdn.example.com/cover.jpg",
        "title": "今天的日落 🌇",
        "type": "video",
        "likeCount": 42,
        "commentCount": 7,
        "viewCount": 0,
        "hasLike": false
      },
      {
        "momentId": 5257117397155840,
        "authorId": 100002,
        "nickname": "小红",
        "avatar": "https://cdn.example.com/avatar/100002.jpg",
        "coverUrl": "https://cdn.example.com/lunch.jpg",
        "title": "今日午餐 🍜",
        "type": "image",
        "likeCount": 18,
        "commentCount": 3,
        "viewCount": 0,
        "hasLike": true
      }
    ]
  },
  "message": "OK"
}
```

### 3.2 关注 Feed 卡片摘要

```
GET /api/social/feed/following/cards
```

#### 请求参数

同 `3.1 推荐 Feed 卡片摘要`（cursor / limit）。

#### 响应参数

同 `3.1`，返回 **FeedCardPageVo**。

### 3.3 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 100003 | 403 | token 失效或未传 |

---

## 4. 评论接口

### 3.1 发布评论或回复

```
POST /api/social/comment/publish
```

#### 请求参数

**CommentPublishDto**（JSON 请求体）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| momentId | Long | 是 | 作品 ID |
| parentCommentId | Long | 否 | 父评论 ID；**顶级评论传 `0`**，回复时传一级评论的 commentId |
| replyCommentId | Long | 否 | 实际回复的那条评论 ID；**顶级评论传 `0`** |
| content | CommentContent | 是 | 评论内容（见下方） |

**CommentContent**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| text | CommentContentText | 是 | 评论文本 |

**CommentContentText**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| text | String | 是 | 评论内容文本 |
| atIds | List\<Long\> | 否 | @的用户 ID 列表 |

#### 发布示例

**顶级评论：**
```json
{
  "momentId": 5257117397155845,
  "parentCommentId": 0,
  "replyCommentId": 0,
  "content": {
    "text": {
      "text": "拍得真好看！",
      "atIds": []
    }
  }
}
```

**回复某条评论：**
```json
{
  "momentId": 5257117397155845,
  "parentCommentId": 100001,
  "replyCommentId": 100001,
  "content": {
    "text": {
      "text": "@小明 同感！",
      "atIds": [100001]
    }
  }
}
```

#### 响应参数

**CommentVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| commentId | Long | 评论 ID |
| momentId | Long | 所属作品 ID |
| momentAuthorId | Long | 作品作者 ID |
| authorId | Long | 评论作者 ID |
| parentCommentId | Long | 父评论 ID（一级评论为 0） |
| replyCommentId | Long | 被回复的评论 ID（一级评论为 0） |
| replyUserId | Long | 被回复的用户 ID（一级评论为 0） |
| commentLevel | Integer | 评论层级：`1`=一级评论，`2`=二级回复 |
| content | CommentContent | 评论内容 |
| status | Integer | 状态：`0`=显示，`1`=隐藏，`2`=删除 |
| likes | Long | 点赞数 |
| replies | Long | 回复数（仅一级评论有效） |
| clientTime | Long | 客户端时间戳（ms） |

```json
{
  "requestId": "322655903800561668",
  "status": 0,
  "state": "OK",
  "content": {
    "commentId": 5257117397155850,
    "momentId": 5257117397155845,
    "momentAuthorId": 100001,
    "authorId": 100002,
    "parentCommentId": 0,
    "replyCommentId": 0,
    "replyUserId": 0,
    "commentLevel": 1,
    "content": {
      "text": {
        "text": "拍得真好看！",
        "atIds": []
      }
    },
    "status": 0,
    "likes": 0,
    "replies": 0,
    "clientTime": 1718000000000
  },
  "message": "OK"
}
```

#### 常见错误

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 300004 | 400 | P_VALUE_ERROR | 参数为空或不合法 |
| 400002 | 422 | R_OPERATION_FAIL | 保存评论失败 |

---

### 3.2 删除评论

```
POST /api/social/comment/delete
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Long | 是 | 要删除的评论 ID（只允许作者本人删除） |

#### 响应

返回 `true` 表示删除成功。

#### 常见错误

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 100003 | 403 | AUTH_TOKEN_INVALID | token 失效 |
| 400002 | 422 | R_OPERATION_FAIL | 删除失败（非本人或不存在） |
| 400003 | 422 | R_NOEXIST_DATA | 评论不存在 |

---

### 3.3 查询作品的一级评论列表

```
GET /api/social/comment/moment/list
```

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| momentId | Long | 是 | — | 作品 ID |
| nextCommentId | Long | 否 | `0` | 游标评论 ID，首次传 `0` |
| limit | int | 否 | `20` | 分页大小，范围 1-50 |

#### 响应参数

**CommentPageVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| momentId | Long | 作品 ID |
| parentCommentId | Long | 父评论 ID（一级评论列表时为 0） |
| nextCommentId | Long | 下一页游标，传 0 表示没有更多 |
| hasMore | Boolean | 是否还有更多数据 |
| commentList | List\<CommentVo\> | 评论列表 |

```json
{
  "requestId": "322655903800561669",
  "status": 0,
  "state": "OK",
  "content": {
    "momentId": 5257117397155845,
    "parentCommentId": 0,
    "nextCommentId": 5257117397155840,
    "hasMore": false,
    "commentList": [
      {
        "commentId": 5257117397155850,
        "momentId": 5257117397155845,
        "momentAuthorId": 100001,
        "authorId": 100002,
        "parentCommentId": 0,
        "replyCommentId": 0,
        "replyUserId": 0,
        "commentLevel": 1,
        "content": {
          "text": {
            "text": "拍得真好看！",
            "atIds": []
          }
        },
        "status": 0,
        "likes": 5,
        "replies": 2,
        "clientTime": 1718000000000
      }
    ]
  },
  "message": "OK"
}
```

---

### 3.4 查询一级评论下的回复列表

```
GET /api/social/comment/reply/list
```

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| parentCommentId | Long | 是 | — | 一级评论 ID |
| nextCommentId | Long | 否 | `0` | 游标评论 ID，首次传 `0` |
| limit | int | 否 | `20` | 分页大小，范围 1-50 |

#### 响应参数

同 `GET /api/social/comment/moment/list`，返回 **CommentPageVo**（`parentCommentId` 为传入的一级评论 ID）。

```json
{
  "requestId": "322655903800561670",
  "status": 0,
  "state": "OK",
  "content": {
    "momentId": 5257117397155845,
    "parentCommentId": 5257117397155850,
    "nextCommentId": 0,
    "hasMore": false,
    "commentList": [
      {
        "commentId": 5257117397155855,
        "momentId": 5257117397155845,
        "momentAuthorId": 100001,
        "authorId": 100001,
        "parentCommentId": 5257117397155850,
        "replyCommentId": 5257117397155850,
        "replyUserId": 100002,
        "commentLevel": 2,
        "content": {
          "text": {
            "text": "@user100002 谢谢！",
            "atIds": [100002]
          }
        },
        "status": 0,
        "likes": 1,
        "replies": 0,
        "clientTime": 1718001000000
      }
    ]
  },
  "message": "OK"
}
```

### 3.5 常见评论错误码

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 300004 | 400 | P_VALUE_ERROR | commentId / momentId 为空或非法 |
| 400002 | 422 | R_OPERATION_FAIL | 操作失败（删除非本人评论等） |
| 400003 | 422 | R_NOEXIST_DATA | 评论/作品不存在 |
| 400013 | 422 | R_ILLEGALITY_CONTENT | 评论内容非法 |

---

## 5. 点赞接口

### 4.1 点赞 / 取消点赞作品

```
POST /api/social/like/moment        # 点赞
POST /api/social/like/moment/cancel # 取消点赞
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| momentId | Long | 是 | 作品 ID |

#### 响应

返回 `true` 表示操作成功。

---

### 4.2 查询作品点赞状态

```
GET /api/social/like/moment/status
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| momentId | Long | 是 | 作品 ID |

#### 响应参数

**LikeStatusVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| targetId | Long | 作品 ID |
| liked | boolean | 当前用户是否已点赞 |

```json
{
  "requestId": "322655903800561671",
  "status": 0,
  "state": "OK",
  "content": {
    "targetId": 5257117397155845,
    "liked": true
  },
  "message": "OK"
}
```

---

### 4.3 点赞 / 取消点赞评论

```
POST /api/social/like/comment        # 点赞
POST /api/social/like/comment/cancel # 取消点赞
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Long | 是 | 评论或回复 ID |

#### 响应

返回 `true` 表示操作成功。

---

### 4.4 查询评论点赞状态

```
GET /api/social/like/comment/status
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Long | 是 | 评论或回复 ID |

#### 响应参数

**LikeStatusVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| targetId | Long | 评论 ID |
| liked | boolean | 当前用户是否已点赞 |

### 4.5 常见点赞错误码

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 300004 | 400 | P_VALUE_ERROR | momentId / commentId 为空 |
| 400001 | 422 | R_ERROR | 重复点赞或取消未点赞 |
| 400003 | 422 | R_NOEXIST_DATA | 作品/评论不存在 |

---

## 6. 动态详情接口

### 5.1 查询动态详情

```
GET /api/social/moment/get
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| momentId | Long | 是 | 动态 ID |

#### 响应参数

**MomentVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| momentId | Long | 动态 ID |
| authorId | Long | 作者 ID |
| content | MomentContent | 动态内容（见下方） |
| latitude | double | 纬度 |
| longitude | double | 经度 |
| country | String | 国家代码，如 `CN` |
| likes | Long | 点赞数 |
| comments | Long | 评论数 |
| hasLike | boolean | 当前用户是否已点赞 |
| status | Integer | 状态：`0`=显示，`1`=隐藏，`2`=删除 |
| createTime | Long | 创建时间戳（ms） |

**MomentContent**（动态内容，按 `type` 字段区分）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 内容类型：`text` / `image` / `video` |
| text | MomentContentText | 条件 | 文本内容（纯文本或图文/视频的描述文字） |
| image | List\<MomentContentImage\> | 条件 | 图片列表（type=image 时必填） |
| video | MomentContentVideo | 条件 | 视频信息（type=video 时必填） |

**MomentContentText**

| 字段 | 类型 | 说明 |
|------|------|------|
| text | String | 文本内容 |
| atIds | List\<Long\> | @的用户 ID 列表 |

**MomentContentImage**

| 字段 | 类型 | 说明 |
|------|------|------|
| imageId | String | 图片 ID |
| imageUrl | String | 图片 URL |
| width | Integer | 宽度（px） |
| height | Integer | 高度（px） |

**MomentContentVideo**

| 字段 | 类型 | 说明 |
|------|------|------|
| videoId | String | 视频 ID |
| videoUrl | String | 视频 URL |
| coverUrl | String | 封面图 URL |
| width | Integer | 宽度（px） |
| height | Integer | 高度（px） |
| duration | Integer | 时长（秒） |

#### 响应示例

```json
{
  "requestId": "322655903800561672",
  "status": 0,
  "state": "OK",
  "content": {
    "momentId": 5257117397155845,
    "authorId": 100001,
    "content": {
      "type": "video",
      "text": {
        "text": "今天的日落 🌇",
        "atIds": []
      },
      "video": {
        "videoId": "v001",
        "videoUrl": "https://cdn.example.com/video.mp4",
        "coverUrl": "https://cdn.example.com/cover.jpg",
        "width": 1920,
        "height": 1080,
        "duration": 15
      }
    },
    "latitude": 22.5431,
    "longitude": 114.0579,
    "country": "CN",
    "likes": 42,
    "comments": 7,
    "hasLike": false,
    "status": 0,
    "createTime": 1718000000000
  },
  "message": "OK"
}
```

---

### 5.2 批量查询动态

```
GET /api/social/moment/list
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| momentIds | List\<Long\> | 条件 | JSON 数组传参，如 `[1,2,3]` |
| momentIdsText | String | 条件 | 逗号分隔的 ID 字符串，如 `"1,2,3"` |

> 两者至少传一个，同时传时取并集去重。

#### 响应参数

**MomentListVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| datas | List\<MomentVo\> | 动态列表 |

---

### 5.3 发布动态

```
POST /api/social/moment/publish
```

#### 请求参数

**MomentPublishDto**（JSON 请求体）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | MomentContent | 是 | 动态内容（见 MomentVo→content 定义） |
| latitude | double | 否 | 纬度 |
| longitude | double | 否 | 经度 |
| country | String | 否 | 国家代码 |
| status | Integer | 否 | 状态 |

```json
{
  "content": {
    "type": "image",
    "text": {
      "text": "今日份的快乐 ✨",
      "atIds": []
    },
    "image": [
      {
        "imageId": "img001",
        "imageUrl": "https://cdn.example.com/photo.jpg",
        "width": 1080,
        "height": 1080
      }
    ]
  },
  "latitude": 22.5431,
  "longitude": 114.0579,
  "country": "CN"
}
```

#### 响应

返回 **MomentVo**（与详情接口相同结构）。

---

### 5.4 删除动态

```
POST /api/social/moment/delete
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| momentId | Long | 是 | 要删除的动态 ID（只允许作者本人删除） |

#### 响应

返回 `true` 表示删除成功。

### 5.5 常见动态错误码

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 300004 | 400 | P_VALUE_ERROR | 参数为空或不合法 |
| 400002 | 422 | R_OPERATION_FAIL | 保存/删除失败 |
| 400003 | 422 | R_NOEXIST_DATA | 动态不存在 |
| 400013 | 422 | R_ILLEGALITY_CONTENT | 内容非法（文本/图片/视频） |

---

## 7. 公共错误码

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 0 | 200 | OK | 处理成功 |
| 300001 | 400 | P_ERROR | 参数错误（通用） |
| 300002 | 400 | P_NOTNULL | 参数值不可为空 |
| 300004 | 400 | P_VALUE_ERROR | 参数值校验不通过 |
| 100002 | 401 | AUTH_LOGIN_FAIL | 登录失败 |
| 100003 | 403 | AUTH_TOKEN_INVALID | token 失效或未传 |
| 200005 | 401 | U_FREEZE | 账户被冻结 |
| 400001 | 422 | R_ERROR | 业务异常（通用） |
| 400002 | 422 | R_OPERATION_FAIL | 操作失败 |
| 400003 | 422 | R_NOEXIST_DATA | 数据不存在 |
| 400013 | 422 | R_ILLEGALITY_CONTENT | 非法内容 |
| 400020 | 422 | R_NOT_SUPPORTED | 不支持的操作 |
| 400005 | 429 | L_FREQUENT_ACCESS | 操作过于频繁 |
| 500 | 500 | SYSTEM_ERROR | 系统内部错误 |
| 500003 | 503 | F_SERVICE_UNAVAILABLE | 下游服务不可用 |

---

## 8. 附录：对象定义

### 8.1 MomentVo（动态 VO，完整结构）

| 字段 | 类型 | 说明 |
|------|------|------|
| momentId | Long | 动态 ID（雪花） |
| authorId | Long | 作者 ID |
| nickname | String | 作者昵称 |
| avatar | String | 作者头像 URL |
| content | MomentContent | 动态内容 |
| latitude | double | 纬度 |
| longitude | double | 经度 |
| country | String | 国家代码 |
| likes | Long | 点赞数 |
| comments | Long | 评论数 |
| hasLike | boolean | 当前用户是否已点赞 |
| status | Integer | 状态 |
| createTime | Long | 创建时间戳（ms） |

### 8.2 FeedCardVo（Feed 卡片摘要）

| 字段 | 类型 | 说明 |
|------|------|------|
| momentId | Long | 作品 ID |
| authorId | Long | 作者 ID |
| nickname | String | 作者昵称 |
| avatar | String | 作者头像 URL |
| coverUrl | String | 封面图 URL |
| title | String | 标题/描述文本 |
| type | String | 内容类型：`video` / `image` / `text` |
| likeCount | Long | 点赞数 |
| commentCount | Long | 评论数 |
| viewCount | Long | 观看数（TODO） |
| hasLike | boolean | 当前用户是否已点赞 |

### 8.3 FeedCardPageVo（Feed 卡片分页响应）

| 字段 | 类型 | 说明 |
|------|------|------|
| nextCursor | Long | 下一页游标，0 表示无更多 |
| hasMore | Boolean | 是否还有更多 |
| datas | List\<FeedCardVo\> | 卡片列表 |

### 8.4 CommentVo（评论 VO）

| 字段 | 类型 | 说明 |
|------|------|------|
| commentId | Long | 评论 ID（雪花） |
| momentId | Long | 所属作品 ID |
| momentAuthorId | Long | 作品作者 ID |
| authorId | Long | 评论作者 ID |
| nickname | String | 评论作者昵称 |
| avatar | String | 评论作者头像 URL |
| parentCommentId | Long | 父评论 ID（一级=0） |
| replyCommentId | Long | 被回复评论 ID（一级=0） |
| replyUserId | Long | 被回复用户 ID（一级=0） |
| commentLevel | Integer | 层级：1=一级，2=二级回复 |
| content | CommentContent | 评论内容 |
| status | Integer | 状态：0=显示，1=隐藏，2=删除 |
| likes | Long | 点赞数 |
| replies | Long | 回复数 |
| clientTime | Long | 客户端时间戳（ms） |

### 8.5 CommentContent（评论内容）

| 字段 | 类型 | 说明 |
|------|------|------|
| text | CommentContentText | 评论文本 |

### 8.6 CommentContentText

| 字段 | 类型 | 说明 |
|------|------|------|
| text | String | 文本内容 |
| atIds | List\<Long\> | @用户 ID 列表 |

### 8.7 Content 嵌套结构（MomentContent）

```
MomentContent
├── type: String            -- "text" / "image" / "video"
├── text: MomentContentText -- 文本（纯文本时必填，图文/视频为描述）
│   ├── text: String
│   └── atIds: List<Long>
├── image: List<MomentContentImage>  -- type=image 时必填
│   ├── imageId: String
│   ├── imageUrl: String
│   ├── width: Integer
│   └── height: Integer
└── video: MomentContentVideo        -- type=video 时必填
    ├── videoId: String
    ├── videoUrl: String
    ├── coverUrl: String
    ├── width: Integer
    ├── height: Integer
    └── duration: Integer
```

### 8.9 FeedPageVo（Feed 分页响应）

| 字段 | 类型 | 说明 |
|------|------|------|
| nextCursor | Long | 下一页游标（momentId），0 表示无更多 |
| hasMore | Boolean | 是否还有更多 |
| datas | List\<MomentVo\> | 动态列表 |

### 8.10 CommentPageVo（评论分页响应）

| 字段 | 类型 | 说明 |
|------|------|------|
| momentId | Long | 作品 ID |
| parentCommentId | Long | 父评论 ID |
| nextCommentId | Long | 下一页游标（commentId），0 表示无更多 |
| hasMore | Boolean | 是否还有更多 |
| commentList | List\<CommentVo\> | 评论列表 |

### 8.11 LikeStatusVo（点赞状态）

| 字段 | 类型 | 说明 |
|------|------|------|
| targetId | Long | 目标 ID（作品或评论） |
| liked | boolean | 是否已点赞 |

### 8.12 完整调用流程示例

```bash
# 1. 登录获取 token
curl -X POST http://localhost:8080/api/user/login/phone \
  -H "Content-Type: application/json" \
  -d '{"phone":"+86-13800138000","code":"123456"}'

# 2. 获取推荐 Feed（第1页，完整版）
curl -X GET "http://localhost:8080/api/social/feed/recommend?cursor=0&limit=10" \
  -H "token: <token>"

# 3. 获取推荐 Feed 卡片摘要（轻量版，供瀑布流使用）
curl -X GET "http://localhost:8080/api/social/feed/recommend/cards?cursor=0&limit=10" \
  -H "token: <token>"

# 4. 获取关注 Feed
curl -X GET "http://localhost:8080/api/social/feed/following?cursor=0&limit=10" \
  -H "token: <token>"

# 5. 获取关注 Feed 卡片摘要
curl -X GET "http://localhost:8080/api/social/feed/following/cards?cursor=0&limit=10" \
  -H "token: <token>"

# 6. 查看动态详情
curl -X GET "http://localhost:8080/api/social/moment/get?momentId=5257117397155845" \
  -H "token: <token>"

# 7. 查询一级评论列表
curl -X GET "http://localhost:8080/api/social/comment/moment/list?momentId=5257117397155845&nextCommentId=0&limit=20" \
  -H "token: <token>"

# 8. 查询一级评论下的回复
curl -X GET "http://localhost:8080/api/social/comment/reply/list?parentCommentId=100001&nextCommentId=0&limit=20" \
  -H "token: <token>"

# 9. 发布评论
curl -X POST http://localhost:8080/api/social/comment/publish \
  -H "Content-Type: application/json" \
  -H "token: <token>" \
  -d '{"momentId":5257117397155845,"parentCommentId":0,"replyCommentId":0,"content":{"text":{"text":"拍得真好看！","atIds":[]}}}'

# 10. 点赞作品
curl -X POST "http://localhost:8080/api/social/like/moment?momentId=5257117397155845" \
  -H "token: <token>"

# 11. 取消点赞作品
curl -X POST "http://localhost:8080/api/social/like/moment/cancel?momentId=5257117397155845" \
  -H "token: <token>"

# 12. 删除评论
curl -X POST "http://localhost:8080/api/social/comment/delete?commentId=5257117397155850" \
  -H "token: <token>"

# 13. 删除动态
curl -X POST "http://localhost:8080/api/social/moment/delete?momentId=5257117397155845" \
  -H "token: <token>"
```

### 8.13 测试账号

| 属性 | 值 |
|------|-----|
| phone | `+86-13800138000` |
| code | `123456` |

### 8.14 服务模块

| 模块 | 说明 |
|------|------|
| `qianyu-social-service` | Feed 推荐流、动态、评论、点赞、关注、访客服务 |
| `qianyu-user-service` | 用户基础信息服务 |
| `qianyu-center-service` | 个人中心聚合服务 |

### 8.15 相关文件

| 文件 | 说明 |
|------|------|
| `FeedController.java` | 推荐 Feed 控制器（`/api/social/feed`）：recommend / following / cards |
| `FeedServiceViewBiz.java` | 推荐 Feed 业务逻辑 |
| `FeedCardVo.java` | Feed 卡片摘要 VO（封面、标题、计数） |
| `FeedCardPageVo.java` | Feed 卡片分页 VO |
| `MomentController.java` | 动态 CRUD 控制器（`/api/social/moment`） |
| `CommentController.java` | 评论 CRUD 控制器（`/api/social/comment`） |
| `LikeController.java` | 点赞控制器（`/api/social/like`） |
| `FollowController.java` | 关注控制器（`/api/social/follow`） |
| `personal-center-api.md` | 个人中心聚合 API |
