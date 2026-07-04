# qianyu-mall-service HTTP 接口自动化测试模块

用 **Spring Boot Test + MockMvc** 对千语 HTTP 接口形态做组件级测试（切片，不启动真实服务器、不连 Dubbo/Nacos/MySQL/Redis）。

## 测试内容

- 示例 Controller：`src/test/java/com/clmcat/qianyu/mall/test/http/HttpDemoController.java`
  - base 路径 `/api/httpdemo`，覆盖 GET / POST / PUT / PATCH / DELETE（HEAD / OPTIONS 由 Spring MVC 自动支持）
  - 参数形式：query、自定义 header、JSON body、表单、文件上传、按 id 定位资源
  - 鉴权：默认开放；`/secure` 用 `@LoginVerify` + `@Token`
  - 错误信封：`ResponseStatus.P_NOTNULL.assertThrowResEx(...)` 抛出后由框架转统一错误信封
- 测试类：`HttpDemoControllerHttpTest`（`@Nested` 分组：GET / 头 / 资源定位 / POST JSON / 表单 / 上传 / PUT·PATCH·DELETE / 鉴权 / 错误信封 / HEAD·OPTIONS）
- 夹具：`TestFixtures`（token 生成、示例 DTO、统一信封状态码常量）

## 项目规范对齐（与通用 Spring 写法的差异）

| 项 | 本模块做法（千语规范） |
|---|---|
| 参数绑定 | `@Params`（query/form/json body/header），**不使用** `@PathVariable`/`@RequestBody`/`@RequestParam` |
| 路径参数 | 禁用 `@PathVariable`；按 id 定位资源用 `@Params("id")` + 路由分段 |
| 文件上传 | `@RequestParam("file") MultipartFile`（项目内 `@RequestParam` 的唯一合法用法） |
| 统一信封 | `{ requestId, status(0=成功), state, content, message }`（断言 `$.status`/`$.content`） |
| 参数校验 | `ResponseStatus.X.assertThrowResEx(cond)` 断言式，**不用** Bean Validation |
| 状态码 | `com.clmcat.framework.webmvc.ResponseStatus`（不存在 `StatusCode`） |
| 鉴权 | `@LoginVerify` + `@Token`；测试用 `DefaultTokenCodec.createToken(uid,"127.0.0.1")` 生成真实 token |
| 测试方式 | `@WebMvcTest` 切片 + `@Import(WebMvcConfiguration.class)` + `@AutoConfigureMockMvc` |

## 测试基础设施（Spring Boot 4.0 适配）

本项目为 **Spring Boot 4.0.6 + JUnit 6**，与通用 Spring Boot 3.x 写法有几处关键差异：

1. **`@WebMvcTest`/`@AutoConfigureMockMvc` 已拆分到独立模块**（SB4 模块化）：
   - 需在 `pom.xml` 增加 `org.springframework.boot:spring-boot-webmvc-test`（test scope）。
   - 导入包由 `org.springframework.boot.test.autoconfigure.web.servlet.*` 改为
     `org.springframework.boot.webmvc.test.autoconfigure.*`。
2. **切片入口配置**：mall-service 是库模块、无 `@SpringBootApplication`，故提供
   `HttpDemoTestConfiguration`（`@SpringBootConfiguration` + `@EnableAutoConfiguration`
   + `@ComponentScan`（测试包）+ `@Import(WebMvcConfiguration.class)`）作为 `@WebMvcTest` 引导入口，
   装回 clmcat-webmvc 的统一信封/鉴权/`@Params` 解析。
3. **关闭 Nacos 配置校验**：`src/test/resources/application.properties` 设
   `spring.cloud.nacos.config.import-check.enabled=false`，否则切片上下文启动时
   `ConfigDataMissingEnvironmentPostProcessor` 因缺 `spring.config.import` 而失败。
4. **登录态**：`TestFixtures.tokenOf(uid)` 用 `DefaultTokenCodec.createToken(uid,"127.0.0.1")`
   生成真实 token 放入 `token` 请求头，走框架真实 `@LoginVerify` 路径。

## 如何运行

```bash
mvn test -pl qianyu-service/qianyu-mall-service -am -Dmaven.compiler.proc=full
```

> 必须带 `-Dmaven.compiler.proc=full`：项目父 pom 未配 `<proc>full</proc>`，JDK 21 默认关闭隐式注解处理，
> 不加该参数 Lombok / MyBatis-Flex APT 不运行，会报大量「找不到符号」。

只跑该测试类：

```bash
mvn test -pl qianyu-service/qianyu-mall-service -am -Dmaven.compiler.proc=full \
  -Dtest=HttpDemoControllerHttpTest
```

## 期望结果

`HttpDemoControllerHttpTest` 全部通过，覆盖：成功信封（`status=0`）、参数错误信封（`P_NOTNULL=300002`）、鉴权失败（4xx）。
