# 动态发布 API 文档

## 目录

1. [通用说明](#1-通用说明)
2. [发布动态](#2-发布动态)
3. [查询动态详情](#3-查询动态详情)
4. [删除动态](#4-删除动态)
5. [公共错误码](#5-公共错误码)
6. [附录：Content 内容结构](#6-附录content-内容结构)

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
| status | int | 状态码，`0` 表示成功 |
| state | String | 状态文本 |
| content | Object | 业务数据 |
| message | String | 说明 |

详细约定见 [docs/api-struct.md](../api-struct.md)。

### 1.3 HTTP 状态码含义

| HTTP | 含义 | 场景 |
|------|------|------|
| 200 | 成功 | 请求正常处理 |
| 400 | 参数错误 | 必填参数缺失、格式错误 |
| 403 | 权限错误 | token 失效 |
| 422 | 业务错误 | 内容不合法、操作失败等 |
| 429 | 限流 | 请求过于频繁 |
| 500 | 系统错误 | 服务端未预期异常 |

### 1.4 接口前缀

```
POST /api/social/moment
```

---

## 2. 发布动态

### 2.1 接口定义

```
POST /api/social/moment/publish
```

### 2.2 请求参数

**MomentPublishDto**（JSON 请求体）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | MomentContent | 是 | 动态内容（结构见附录） |
| latitude | double | 否 | 纬度 |
| longitude | double | 否 | 经度 |
| country | String | 否 | 国家代码，如 `CN` |
| status | Integer | 否 | 状态，默认 `0` |

### 2.3 Content 快速参考

`MomentContent` 按 `type` 字段区分三种模式：

| type 值 | 必填子字段 | 说明 |
|---------|-----------|------|
| `text` | `text` | 纯文本动态，attach 图片或视频 |
| `image` | `text` + `image[]` | 图文动态，至少一张图片 |
| `video` | `text` + `video` | 视频动态，需提供视频信息和封面 |

### 2.4 请求示例

**纯文本：**
```json
{
  "content": {
    "type": "text",
    "text": {
      "text": "今天天气真好！",
      "atIds": []
    }
  }
}
```

**图文：**
```json
{
  "content": {
    "type": "image",
    "text": {
      "text": "今日份的快乐 ✨",
      "atIds": [100002]
    },
    "image": [
      {
        "imageId": "img_001",
        "imageUrl": "https://cdn.example.com/photo1.jpg",
        "width": 1080,
        "height": 1080
      },
      {
        "imageId": "img_002",
        "imageUrl": "https://cdn.example.com/photo2.jpg",
        "width": 1920,
        "height": 1080
      }
    ]
  },
  "latitude": 22.5431,
  "longitude": 114.0579,
  "country": "CN"
}
```

**视频：**
```json
{
  "content": {
    "type": "video",
    "text": {
      "text": "今天的日落 🌇",
      "atIds": []
    },
    "video": {
      "videoId": "v_001",
      "videoUrl": "https://cdn.example.com/video.mp4",
      "coverUrl": "https://cdn.example.com/cover.jpg",
      "width": 1920,
      "height": 1080,
      "duration": 15
    }
  },
  "latitude": 22.5431,
  "longitude": 114.0579
}
```

### 2.5 响应参数

**MomentVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| momentId | Long | 动态 ID（雪花算法生成） |
| authorId | Long | 作者 ID |
| content | MomentContent | 发布的内容 |
| latitude | double | 纬度 |
| longitude | double | 经度 |
| country | String | 国家代码 |
| likes | Long | 点赞数（新发布为 0） |
| comments | Long | 评论数（新发布为 0） |
| hasLike | boolean | 是否已点赞（本人查看为 false） |
| status | Integer | 状态：`0`=显示，`1`=隐藏，`2`=删除 |
| createTime | Long | 创建时间戳（ms） |

### 2.6 响应示例

```json
{
  "requestId": "322655903800561700",
  "status": 0,
  "state": "OK",
  "content": {
    "momentId": 5257117397155900,
    "authorId": 100001,
    "content": {
      "type": "image",
      "text": {
        "text": "今日份的快乐 ✨",
        "atIds": [100002]
      },
      "image": [
        {
          "imageId": "img_001",
          "imageUrl": "https://cdn.example.com/photo1.jpg",
          "width": 1080,
          "height": 1080
        }
      ]
    },
    "latitude": 22.5431,
    "longitude": 114.0579,
    "country": "CN",
    "likes": 0,
    "comments": 0,
    "hasLike": false,
    "status": 0,
    "createTime": 1718000000000
  },
  "message": "OK"
}
```

### 2.7 校验规则

| 规则 | 说明 |
|------|------|
| content 必填 | content 本身及其 type 字段均不能为空 |
| type 合法值 | 仅支持 `text` / `image` / `video` |
| 内容互斥 | text / image / video 至少有一个有值 |
| 图片校验 | type=image 时，每张图片必须提供 imageUrl、width、height |
| 视频校验 | type=video 时，必须提供 videoUrl、coverUrl、width、height、duration |
| 文本兜底 | 纯文本（无图片/视频）时，文本内容不能为空 |

### 2.8 常见错误

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 300004 | 400 | P_VALUE_ERROR | 参数为空或不合法 |
| 100003 | 403 | AUTH_TOKEN_INVALID | token 失效 |
| 400013 | 422 | R_ILLEGALITY_CONTENT | 内容非法（文本/图片/视频） |
| 400002 | 422 | R_OPERATION_FAIL | 保存失败 |

---

## 3. 查询动态详情

### 3.1 单条详情

```
GET /api/social/moment/get
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| momentId | Long | 是 | 动态 ID |

#### 响应参数

**MomentVo**（同发布响应结构）

```json
{
  "requestId": "322655903800561701",
  "status": 0,
  "state": "OK",
  "content": {
    "momentId": 5257117397155845,
    "authorId": 100001,
    "content": {
      "type": "video",
      "text": { "text": "今天的日落 🌇", "atIds": [] },
      "video": {
        "videoId": "v_001",
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

> `hasLike` 表示当前登录用户是否已点赞该动态，与 `GET /api/social/like/moment/status` 结果一致。

### 3.2 批量查询

```
GET /api/social/moment/list
```

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| momentIds | List\<Long\> | 条件 | JSON 数组，如 `[1,2,3]` |
| momentIdsText | String | 条件 | 逗号分隔字符串，如 `"1,2,3"` |

> 两者至少传一个，同时传时取并集去重。

#### 响应参数

**MomentListVo**

| 字段 | 类型 | 说明 |
|------|------|------|
| datas | List\<MomentVo\> | 动态列表（按请求 ID 顺序返回） |

```json
{
  "requestId": "322655903800561702",
  "status": 0,
  "state": "OK",
  "content": {
    "datas": [
      { "momentId": 1, ... },
      { "momentId": 2, ... }
    ]
  },
  "message": "OK"
}
```

### 3.3 常见错误

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 300004 | 400 | momentId 为空 |
| 100003 | 403 | token 失效 |
| 400003 | 422 | 动态不存在 |

---

## 4. 删除动态

### 4.1 接口定义

```
POST /api/social/moment/delete
```

### 4.2 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| momentId | Long | 是 | 要删除的动态 ID |

> 只允许作者本人删除自己的动态，非作者调用会返回 422 错误。

### 4.3 响应

返回 `true` 表示删除成功。

### 4.4 常见错误

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 300004 | 400 | P_VALUE_ERROR | momentId 为空 |
| 100003 | 403 | AUTH_TOKEN_INVALID | token 失效 |
| 400003 | 422 | R_NOEXIST_DATA | 动态不存在 |
| 400002 | 422 | R_OPERATION_FAIL | 删除失败（非本人操作） |

---

## 5. 公共错误码

| 错误码 | HTTP | state | 说明 |
|--------|------|-------|------|
| 0 | 200 | OK | 处理成功 |
| 300001 | 400 | P_ERROR | 参数错误（通用） |
| 300004 | 400 | P_VALUE_ERROR | 参数值校验不通过 |
| 100003 | 403 | AUTH_TOKEN_INVALID | token 失效 |
| 400001 | 422 | R_ERROR | 业务异常（通用） |
| 400002 | 422 | R_OPERATION_FAIL | 操作失败 |
| 400003 | 422 | R_NOEXIST_DATA | 数据不存在 |
| 400013 | 422 | R_ILLEGALITY_CONTENT | 非法内容 |
| 400005 | 429 | L_FREQUENT_ACCESS | 操作过于频繁 |
| 500 | 500 | SYSTEM_ERROR | 系统内部错误 |

---

## 6. 附录：Content 内容结构

### 6.1 MomentContent（动态内容）

```
MomentContent
├── type: String                   -- 内容类型："text" / "image" / "video"
├── text: MomentContentText        -- 文本（纯文本必填，图文/视频作描述）
│   ├── text: String               -- 文本内容，支持表情符号
│   └── atIds: List<Long>          -- @的用户 ID 列表
├── image: List<MomentContentImage> -- 图片列表（type=image 时必填）
│   ├── imageId: String            -- 图片 ID
│   ├── imageUrl: String           -- 图片 URL（CDN 地址）
│   ├── width: Integer             -- 宽度（px）
│   └── height: Integer            -- 高度（px）
└── video: MomentContentVideo      -- 视频信息（type=video 时必填）
    ├── videoId: String            -- 视频 ID
    ├── videoUrl: String           -- 视频播放 URL
    ├── coverUrl: String           -- 封面图 URL
    ├── width: Integer             -- 宽度（px）
    ├── height: Integer            -- 高度（px）
    └── duration: Integer          -- 时长（秒）
```

### 6.2 类型说明

| type | 存储结构 | 前端展示 |
|------|---------|---------|
| `text` | `{ type, text }` | 纯文本内容，可带 @ 用户 |
| `image` | `{ type, text, image[] }` | 图片网格（1~9 张），上方有描述文字 |
| `video` | `{ type, text, video }` | 视频播放器 + 封面图，下方有描述文字 |

### 6.3 curl 示例

```bash
# 1. 登录获取 token
curl -X POST http://localhost:8080/api/user/login/phone \
  -H "Content-Type: application/json" \
  -d '{"phone":"+86-13800138000","code":"123456"}'

# 2. 发布图文动态
curl -X POST http://localhost:8080/api/social/moment/publish \
  -H "Content-Type: application/json" \
  -H "token: <token>" \
  -d '{
    "content": {
      "type": "image",
      "text": { "text": "今日份的快乐 ✨", "atIds": [] },
      "image": [
        { "imageId": "img_001", "imageUrl": "https://cdn.example.com/photo.jpg", "width": 1080, "height": 1080 }
      ]
    },
    "latitude": 22.5431,
    "longitude": 114.0579,
    "country": "CN"
  }'

# 3. 发布视频动态
curl -X POST http://localhost:8080/api/social/moment/publish \
  -H "Content-Type: application/json" \
  -H "token: <token>" \
  -d '{
    "content": {
      "type": "video",
      "text": { "text": "今天的日落 🌇", "atIds": [] },
      "video": {
        "videoId": "v_001",
        "videoUrl": "https://cdn.example.com/video.mp4",
        "coverUrl": "https://cdn.example.com/cover.jpg",
        "width": 1920, "height": 1080, "duration": 15
      }
    }
  }'

# 4. 查询动态详情
curl -X GET "http://localhost:8080/api/social/moment/get?momentId=5257117397155900" \
  -H "token: <token>"

# 5. 删除动态
curl -X POST "http://localhost:8080/api/social/moment/delete?momentId=5257117397155900" \
  -H "token: <token>"
```

### 6.4 测试账号

| 属性 | 值 |
|------|-----|
| phone | `+86-13800138000` |
| code | `123456` |

### 6.5 相关文件

| 文件 | 说明 |
|------|------|
| `MomentController.java` | 动态 CRUD 控制器（`/api/social/moment`） |
| `MomentServiceViewBiz.java` | 动态视图层业务逻辑 |
| `MomentServiceBiz.java` | 动态 Dubbo 服务实现 |
| `MomentPublishDto.java` | 发布参数 DTO |
| `MomentVo.java` | 动态响应 VO |
| `MomentSupport.java` | 动态转换、校验工具类 |
| `MomentContent` / `MomentContentText` / `MomentContentImage` / `MomentContentVideo` | 内容结构 DTO |
| `social-feed-api.md` | Feed 推荐流、评论、点赞 API |
