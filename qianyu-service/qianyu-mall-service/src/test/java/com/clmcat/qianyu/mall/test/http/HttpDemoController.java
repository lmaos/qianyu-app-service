package com.clmcat.qianyu.mall.test.http;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import lombok.Data;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * HTTP 接口自动化测试 —— 示例 Controller（仅存在于测试源码，不进入生产 jar）。
 *
 * <p>集中演示千语框架下「所有常见 HTTP 形态」，供 {@link HttpDemoControllerHttpTest} 用 MockMvc 组件级测试覆盖：
 * <ul>
 *   <li>HTTP 方法：GET / POST / PUT / PATCH / DELETE（HEAD / OPTIONS 由 Spring MVC 对 GET 映射自动支持）</li>
 *   <li>参数形式：query、自定义 header、JSON body、表单、文件上传，以及「按 id 定位资源」</li>
 *   <li>鉴权：默认开放；{@code /secure} 方法用 {@link LoginVerify} 强制登录</li>
 *   <li>统一信封：成功返回值由框架自动包装；参数错误经 {@link ResponseStatus#assertThrowResEx} 抛出后转统一错误信封</li>
 * </ul>
 *
 * <p><b>项目规范说明</b>：千语禁用 {@code @PathVariable}，所有参数统一走 {@link Params}（query/form/json body/header）；
 * 故「路径参数」覆盖项以「多段路径路由 + {@code @Params("id")} 查询参数」实现，而非 {@code @PathVariable}。
 * 文件上传是项目内 {@code @RequestParam} 的唯一合法用法（{@code MultipartFile}，见生产代码 StorageController）。
 *
 * <p>返回值统一为 {@link Map} / 对象，由框架 {@code ComponentResponseHandler} 自动包成
 * {@code { requestId, status(0=成功), state, content, message }} 信封。
 */
@ApiController
@RequestMapping("/api/httpdemo")
public class HttpDemoController {

    // ============================== GET ==============================

    /** GET + query 参数回显。示例：/api/httpdemo/echo?name=千语&age=18 */
    @GetMapping("/echo")
    public Map<String, Object> echo(@Params("name") String name, @Params("age") Integer age) {
        Map<String, Object> echo = new LinkedHashMap<>();
        echo.put("method", "GET");
        echo.put("name", name);
        echo.put("age", age);
        return echo;
    }

    /** GET + 自定义请求头。示例：请求头 X-Demo: hello */
    @GetMapping("/headers")
    public Map<String, Object> header(
            @Params(name = "X-Demo", scope = Params.ParamsScope.HEADER, required = false) String xDemo) {
        Map<String, Object> echo = new LinkedHashMap<>();
        echo.put("method", "GET");
        echo.put("scope", "HEADER");
        echo.put("X-Demo", xDemo);
        return echo;
    }

    /** GET + 按 id 定位资源（项目规范：id 走 @Params query，不用 @PathVariable）。示例：/api/httpdemo/items?id=123 */
    @GetMapping("/items")
    public Map<String, Object> getItem(@Params("id") Long id) {
        ResponseStatus.P_NOTNULL.assertThrowResEx(id == null || id <= 0);
        Map<String, Object> echo = new LinkedHashMap<>();
        echo.put("method", "GET");
        echo.put("id", id);
        echo.put("name", "示例商品-" + id);
        return echo;
    }

    /** GET + 强制登录：{@link LoginVerify} + {@link Token} 注入当前用户 ID。 */
    @GetMapping("/secure")
    @LoginVerify
    public Map<String, Object> secure(@Token Long userId) {
        Map<String, Object> echo = new LinkedHashMap<>();
        echo.put("method", "GET");
        echo.put("loginVerify", true);
        echo.put("userId", userId);
        return echo;
    }

    /** GET + 参数校验演示：name 为空即抛 P_NOTNULL，框架转统一错误信封。 */
    @GetMapping("/validate")
    public Map<String, Object> validate(@Params("name") String name) {
        ResponseStatus.P_NOTNULL.assertThrowResEx(name == null || name.isBlank());
        Map<String, Object> echo = new LinkedHashMap<>();
        echo.put("validated", true);
        echo.put("name", name);
        return echo;
    }

    // ============================== POST ==============================

    /** POST + JSON 请求体（整体绑定到 DemoDto）。Content-Type: application/json */
    @PostMapping("/items/body")
    public DemoDto postBody(@Params DemoDto dto) {
        return dto;
    }

    /** POST + 表单（按字段名绑定到 DemoDto）。Content-Type: application/x-www-form-urlencoded */
    @PostMapping("/items/form")
    public DemoDto postForm(@Params DemoDto dto) {
        return dto;
    }

    /** POST + 文件上传（multipart/form-data）。file 用 @RequestParam，伴随字段用 @Params。 */
    @PostMapping("/files/upload")
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
            @Params("desc") String desc) {
        Map<String, Object> echo = new LinkedHashMap<>();
        echo.put("method", "POST");
        echo.put("desc", desc);
        echo.put("originalFilename", file.getOriginalFilename());
        echo.put("contentType", file.getContentType());
        echo.put("size", file.getSize());
        echo.put("empty", file.isEmpty());
        return echo;
    }

    // ============================== PUT / PATCH / DELETE ==============================

    /** PUT + 按 id 更新（id 走 query，body 走 JSON）。 */
    @PutMapping("/items")
    public DemoDto putItem(@Params("id") Long id, @Params DemoDto dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx(id == null || id <= 0);
        return dto;
    }

    /** PATCH + 按 id 局部更新。 */
    @PatchMapping("/items")
    public DemoDto patchItem(@Params("id") Long id, @Params DemoDto dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx(id == null || id <= 0);
        return dto;
    }

    /** DELETE + 按 id 删除。 */
    @DeleteMapping("/items")
    public Boolean deleteItem(@Params("id") Long id) {
        ResponseStatus.P_NOTNULL.assertThrowResEx(id == null || id <= 0);
        return Boolean.TRUE;
    }

    // ============================== 请求体模型 ==============================

    /** JSON / 表单绑定对象。 */
    @Data
    public static class DemoDto {
        private String name;
        private Integer age;
        private Boolean active;
        private List<String> tags;
    }
}
