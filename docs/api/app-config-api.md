# App 启动配置 API

## 1. 概述

App 启动时调用的全局配置接口，返回客户端运行所需的动态参数（功能开关、UI 配置、业务限制等），无需登录。

后端通过 `app_config` 表管理配置，运维可直接修改数据库，无需重启服务或发版。

- **基础路径**：`/api/app/setting`
- **认证**：不需要
- **响应格式**：`{status, state, content, message}`（由 `@ApiController` 自动包装）

---

## 2. 接口

### 2.1 获取启动配置

```
GET /api/app/setting/getconfig
```

无参数。

响应 `content` 为按 section 分组的配置 Map：

```json
{
  "features": {
    "live": {"enabled": true, "entry_visible": true},
    "gift": {"enabled": false},
    "moment_comment": {"enabled": true}
  },
  "ui": {
    "home_tabs": ["recommend", "follow", "live", "nearby"],
    "bottom_nav": [
      {"key": "home", "icon": "home", "label": "首页"},
      {"key": "discover", "icon": "discover", "label": "发现"},
      {"key": "publish", "special": true},
      {"key": "message", "icon": "msg", "label": "消息"},
      {"key": "me", "icon": "me", "label": "我的"}
    ]
  },
  "limits": {
    "publish": {"max_image_count": 9, "max_video_duration": 60, "max_text_length": 2000}
  },
  "upload": {
    "oss": {"endpoint": "https://oss.clmcat.com", "max_file_size": 104857600}
  },
  "app_update": {
    "version": {"forced_version": "2.1.0", "latest_version": "2.2.0", "update_url": "https://app.clmcat.com/download"}
  },
  "maintenance": {
    "status": {"in_maintenance": false, "message": ""}
  }
}
```

---

## 3. 数据模型

### 3.1 表结构

```sql
CREATE TABLE app_config (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    section     VARCHAR(32)  NOT NULL COMMENT '配置段',
    config_key  VARCHAR(64)  NOT NULL COMMENT '配置键',
    config_value TEXT        NOT NULL COMMENT '配置值（JSON）',
    value_type  VARCHAR(16)  NOT NULL DEFAULT 'string' COMMENT '值类型：string / number / boolean / json',
    description VARCHAR(255) NOT NULL DEFAULT '' COMMENT '说明',
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_section_key (section, config_key)
) COMMENT='App 启动配置表';
```

### 3.2 设计说明

- **section**：配置段名，API 返回时按此字段分组，作为 JSON 的一级 key。如 `features`、`ui`、`limits`
- **config_key**：段内唯一键，作为 JSON 的二级 key
- **config_value**：统一存 JSON 字符串，后端根据 `value_type` 决定是否反序列化
  - `json` 类型 → 解析为 Map/List 再返回
  - 其他类型 → 原样返回字符串
- **一行就是一个原子配置项**：改一个开关只 UPDATE 一行，不牵连其他配置

### 3.3 常见配置段

| section | 用途 | 示例 |
|---------|------|------|
| `features` | 功能开关（灰度/上线控制） | `live` / `gift` / `moment_comment` |
| `ui` | UI 布局配置 | `home_tabs` / `bottom_nav` |
| `limits` | 业务限制参数 | `publish` / `comment` |
| `upload` | 上传相关配置 | `oss` / `cos` |
| `app_update` | App 版本更新信息 | `version` |
| `maintenance` | 系统维护状态 | `status` |

---

## 4. 客户端使用建议

```javascript
// App 启动时调用一次，缓存到本地
const config = await api.get('/api/app/setting/getconfig');

// 按 section 取用
if (config.features?.live?.enabled) {
    showLiveEntry(); // 显示直播入口
}

// 检查维护状态
if (config.maintenance?.status?.in_maintenance) {
    showMaintenancePage(config.maintenance.status.message);
}

// 检查强制更新
const update = config.app_update?.version;
if (compareVersion(update.forced_version) > 0) {
    showForceUpdate(update.update_url);
}
```

---

## 5. 注意事项

- 此接口 **无需登录**，App 启动时第一个调用
- 后续如需用户维度的差异化配置，可加 token 参数做扩展
- 新增配置段只需 INSERT 一行，**不改表、不改代码**
- 建议客户端缓存此配置，避免每次启动重复请求
