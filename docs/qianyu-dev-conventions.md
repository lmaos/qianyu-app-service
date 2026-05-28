# Qianyu 开发规范与设计风格

> 会话内整理文档。用于后续实现时快速读取并对齐当前仓库的架构原则、开发风格与 `clmcat-webmvc` 用法。

## 1. 总体架构原则

### 1.1 模块职责

- `qianyu-app-service`
  - 管理全局依赖配置、顶层聚合与版本策略。
- `qianyu-dependencies`
  - 管理真实可导入的依赖包。
  - 其中：
    - `qianyu-web-dependencies`：Web 开发所需外部依赖集合
    - `qianyu-allapi-dependencies`：全部 API 模块集合
    - `qianyu-allservice-dependencies`：全部 service 模块集合
- `qianyu-core`
  - 放共享能力、公共适配、公共基础设施。
  - 这里的能力应尽量是“有依赖才生效”的条件式支持。
- `qianyu-api`
  - 每个模块对外暴露的 API 契约层。
  - 模块之间调用，只能通过 `qianyu-api` 暴露的接口完成。
  - 具体实现由 Dubbo RPC 提供。
- `qianyu-service`
  - 业务实现层。
- `qianyu-boot`
  - 启动层，只负责启动与编排，不承载业务规则。

### 1.2 依赖方向

- Web 开发链路：
  - `service -> qianyu-web-dependencies -> qianyu-core`
- 模块间调用链路：
  - `service -> qianyu-api`
  - 禁止直接依赖其他模块的 service 实现。
- 启动链路：
  - `qianyu-boot -> qianyu-service-*`

### 1.3 启动拓扑

- 开发 / 单体部署：
  - `qianyu-single-boot -> qianyu-allservice-dependencies`
- 微服务部署：
  - `qianyu-{module}-boot -> qianyu-{module}-service`

## 2. 开发风格

### 2.1 包与分层风格

- 新业务按 `com.clmcat.qianyu.{domain}.{module}` 分包。
- HTTP 侧一般按以下结构组织：
  - `controller`
  - `service`
  - `mapper`
  - `support`
  - `model.dto`
  - `model.vo`
  - `model.entity`
  - `model.entity.status`
- 常见职责链路：
  - `Controller -> ViewBiz -> Biz -> Mapper`

### 2.2 API 与业务边界

- HTTP Controller 面向前端，返回 VO。
- Dubbo API 面向模块间调用，返回 DTO / 基础类型。
- 新增跨模块能力时：
  1. 先定义 `qianyu-api`
  2. 再由对应 service 实现 Dubbo 服务
  3. 禁止直接引用别的模块业务实现类

### 2.3 代码风格要求

- 参考现有结构与命名，不随意改已有方法定义。
- 除非是明确 BUG，否则尽量不破坏既有签名。
- 可以新增支持方法，但优先复用已有模式。
- 新写公共方法、HTTP 接口、RPC 接口应增加注释，重点说明：
  - 参数含义
  - 游标含义
  - 登录态来源
  - 返回值用途

### 2.4 数据与文档风格

- HTTP 返回优先走统一响应链路。
- 前端与 AI 可读文档优先提供标准 OpenAPI JSON。
- OpenAPI 适配逻辑放在 `qianyu-core`，外部依赖放在 `qianyu-web-dependencies`。

## 3. clmcat-webmvc 使用规则

参考：

- `clmcat-webmvc/README.md`
- `com.clmcat.framework.webmvc.anns.*`

### 3.1 `@ApiController`

用途：

- 推荐的 API 控制器注解。
- 等价于 `@RestController`，并进入统一响应包装链路。

规则：

- HTTP API Controller 优先使用 `@ApiController`，不要随意改成普通 `@RestController`。
- 返回普通对象时，会自动包装为统一 JSON 响应结构。
- 可选配置：
  - `entityKey`
  - `statusAdapter`
  - `resultAdapterName`
  - `logMode`

适用建议：

- 业务 HTTP 接口默认都使用它。
- 如果要返回文件流/字节流，再考虑 `CustomResponseEntity`。

### 3.2 `@Params`

用途：

- 统一请求参数注入。
- 支持简单值和对象参数。

支持来源：

- query
- form
- JSON body
- header
- cookie
- client IP
- request attribute

简单参数示例：

```java
public Object detail(@Params("id") Long id) {
    return id;
}
```

对象参数示例：

```java
public Object save(@Params UserSaveDto dto) {
    return dto;
}
```

对象装配规则：

1. `GET` / form 请求按字段名注入
2. JSON body 先反序列化对象
3. query / form / 自定义参数可覆盖同名字段
4. `PUT` / `PATCH` / `DELETE` 也支持 body

字段级 `@Params`：

```java
public class DeviceDto {
    @Params(name = "device-id", scope = Params.ParamsScope.HEADER, required = false)
    private String deviceId;
}
```

`scope` 说明：

- `PARAM`：默认，优先 body/form/query/custom 参数
- `HEADER`：请求头
- `COOKIE`：cookie
- `IP`：客户端 IP
- `REQUEST`：`request.getAttribute(...)`
- `NONE`：只走原始 request parameter

项目内使用建议：

- 普通查询参数、表单参数、JSON DTO，优先统一使用 `@Params`
- request 上下文对象，使用 `@Params(scope = REQUEST)`
- 需要从 header/cookie 注入的字段，优先在 DTO 字段上标记 `@Params`

### 3.3 `@Token`

用途：

- 注入 token 原文、用户 ID 或 token 解析信息。

常见用法：

```java
public Object profile(@Token Long userId) {
    return userId;
}
```

默认参数名：

- token：`token`
- userId：`userId`

规则：

- `@Token long userId` / `@Token Long userId` 是项目中最常见的登录用户注入方式。
- Controller 里需要当前登录用户时，优先用 `@Token`，不要自己手写取 header 逻辑。
- OpenAPI 文档中应把它视为登录态来源，而不是普通业务参数。

### 3.4 `@LoginVerify`

用途：

- 启用登录校验，可标注在类或方法上。

示例：

```java
@ApiController
@LoginVerify
public class UserController {
}
```

可配置项：

- `token`
- `userId`
- `mustLogin`
- `loginVerify`
- `loginError`

规则：

- 需要登录才能访问的接口，统一使用 `@LoginVerify`
- 如果整个 controller 都需要登录，可直接标注在类上
- 单个方法例外时，再按方法粒度控制

### 3.5 `@NoLoginVerify`

用途：

- 在已有登录校验上下文中显式声明某接口不需要登录。

规则：

- 当 controller 类上已经有 `@LoginVerify`，但个别接口需要公开访问时使用。

### 3.6 `@GetLocale`

用途：

- 注入当前请求语言。

支持：

- `@GetLocale Locale locale`
- `@GetLocale String locale`

解析顺序（按框架 README）大致为：

1. `request attribute: userLocale`
2. `request attribute: userLanguage`
3. `header/param: userLanguage`
4. `header/param: locale`
5. `Accept-Language`
6. `request.getLocale()`
7. 默认 locale

规则：

- 业务需要当前语言时，统一使用 `@GetLocale`
- 不要自己重复解析 `Accept-Language`

## 4. 响应与状态风格

### 4.1 统一响应

- `@ApiController` 下，普通返回值会进入统一响应包装。
- 常用状态定义：
  - `ResponseStatus`
  - 各模块自定义 `Status`

### 4.2 错误状态使用

- 公共通用状态优先复用 `ResponseStatus`
- 模块内业务状态放在模块自己的 `model.entity.status.Status`
- 业务判断失败时优先用 `assertThrowResEx(...)`

## 5. OpenAPI / AI 文档规范

- 目标产物优先是 OpenAPI JSON，而不是只做网页展示。
- 已接入方向：
  - 外部依赖放 `qianyu-web-dependencies`
  - `qianyu-core` 用条件配置做注解适配
- 自定义注解文档映射原则：
  - `@LoginVerify` -> security
  - `@Token` -> 登录态注入，不当作普通业务参数
  - `@Params` -> query/header/cookie 等参数定义
- 新增 HTTP 接口时，优先使用明确的 `@GetMapping`、`@PostMapping`、`@DeleteMapping` 等，而不是泛化的 `@RequestMapping`
- 新增 HTTP 接口时，应补齐 OpenAPI 可见说明：
  - 类级 `@Tag`
  - 方法级 `@Operation`
  - 必要时补 `@RequestBody` / `@ParameterObject`
  - 参数说明需要能在 OpenAPI 页面中直接看到

## 6. 实现时的落地准则

新增功能时，优先按下面顺序思考：

1. 是否属于模块对外能力？
   - 是：先定义 `qianyu-api`
2. 是否属于公共基础能力？
   - 是：放 `qianyu-core`
3. 是否属于外部依赖集合？
   - 是：放 `qianyu-web-dependencies`
4. 是否属于具体业务实现？
   - 是：放对应 `qianyu-service`
5. 是否属于启动编排？
   - 是：放 `qianyu-boot`

## 7. 以后优先遵守的具体习惯

- 控制器优先使用 `@ApiController`
- 常规入参优先使用 `@Params`
- 当前登录用户优先使用 `@Token`
- 登录校验统一使用 `@LoginVerify`
- 当前语言统一使用 `@GetLocale`
- 跨模块调用只能走 `qianyu-api` + Dubbo RPC
- 公共 Web / OpenAPI / 通用适配逻辑尽量放 `qianyu-core`
- 外部 Web 依赖尽量收敛到 `qianyu-web-dependencies`
- 新增 API 接口时自动补接口说明与参数说明

## 雪花算法使用: 
```java
com.clmcat.basics.commons.snowflake.CustomSnowflake snowflake =
com.clmcat.qianyu.core.snowflake.SnowflakeSupport.createSnowflake(42, 10, 11);

long id = snowflake.nextId();

```



createSnowflake(int timeStrategyBit, int machineStrategyBit, int sequenceStrategyBit)

- timeStrategyBit：时间戳位数.
- machineStrategyBit：机器位数.
- sequenceStrategyBit：序列号位数.


SnowflakeSupport.parseTimeBySnowflake(CustomSnowflake snowflake, long id)
- 解析出 id 中的时间戳部分，返回一个 long 时间戳，id的时间。
- 开发建议， 雪花ID的时间与入库 create_time 保持一致。
- 时间游标分页: 可简化用 雪花id暂时分页。