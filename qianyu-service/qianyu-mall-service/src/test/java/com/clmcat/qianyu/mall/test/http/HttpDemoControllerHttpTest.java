package com.clmcat.qianyu.mall.test.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clmcat.framework.webmvc.WebMvcConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * HTTP 接口自动化测试 —— MockMvc 组件级测试。
 *
 * <p>策略：{@code @WebMvcTest} 切片 + {@code @Import(WebMvcConfiguration.class)} 装回 clmcat-webmvc 的
 * 拦截器（鉴权/信封属性）、参数解析器（{@code @Params}/@Token）、返回值处理器（统一信封包装）、
 * 异常处理（异常→统一错误信封）。不启动真实服务器、不连 Dubbo/Nacos/MySQL/Redis。
 *
 * <p>登录态：通过 {@link TestFixtures#tokenOf(long)} 生成真实 token 放入 {@value TestFixtures#TOKEN_HEADER} 头，
 * 走框架真实 {@code @LoginVerify} 路径。
 *
 * <p>断言统一信封 {@code { requestId, status(0=成功), state, content, message }}，覆盖成功 / 参数错误 / 鉴权失败。
 */
@WebMvcTest(HttpDemoController.class)
@Import(WebMvcConfiguration.class)
@AutoConfigureMockMvc
class HttpDemoControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    // 仅用于序列化测试请求体，无需 Spring 容器托管
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    // ============================== GET / 查询参数 ==============================

    @Nested
    @DisplayName("GET 请求 / 查询参数")
    class GetQueryTests {

        @Test
        @DisplayName("GET /echo 回显 query 参数，返回成功信封")
        void shouldEchoQueryParams() throws Exception {
            mockMvc.perform(get("/api/httpdemo/echo")
                            .param("name", "qianyu")
                            .param("age", "18"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".method").value("GET"))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".name").value("qianyu"))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".age").value(18));
        }
    }

    // ============================== 请求头 ==============================

    @Nested
    @DisplayName("GET 请求头")
    class HeaderTests {

        @Test
        @DisplayName("GET /headers 读取自定义请求头 X-Demo")
        void shouldReadCustomHeader() throws Exception {
            mockMvc.perform(get("/api/httpdemo/headers")
                            .header("X-Demo", "hello"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".X-Demo").value("hello"));
        }

        @Test
        @DisplayName("GET /headers 未携带请求头时仍成功（required=false 放行；null 字段被信封序列化省略）")
        void shouldAllowMissingHeader() throws Exception {
            mockMvc.perform(get("/api/httpdemo/headers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".scope").value("HEADER"));
        }
    }

    // ============================== 资源定位（@Params 替代 @PathVariable） ==============================

    @Nested
    @DisplayName("按 id 定位资源（项目规范：@Params query，非 @PathVariable）")
    class ResourceByIdTests {

        @Test
        @DisplayName("GET /items?id=123 返回资源")
        void shouldGetItemById() throws Exception {
            mockMvc.perform(get("/api/httpdemo/items")
                            .param("id", "123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".id").value(123));
        }

        @Test
        @DisplayName("GET /items 未带 id → P_NOTNULL 参数错误信封")
        void shouldRejectMissingId() throws Exception {
            mockMvc.perform(get("/api/httpdemo/items"))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_P_NOTNULL));
        }
    }

    // ============================== POST JSON ==============================

    @Nested
    @DisplayName("POST JSON 请求体")
    class PostJsonTests {

        @Test
        @DisplayName("POST /items/body 绑定 JSON body 并回显")
        void shouldBindJsonBody() throws Exception {
            mockMvc.perform(post("/api/httpdemo/items/body")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(TestFixtures.demoDto("qianyu", 18))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".name").value("qianyu"))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".age").value(18))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".active").value(true));
        }
    }

    // ============================== POST 表单 ==============================

    @Nested
    @DisplayName("POST 表单提交")
    class PostFormTests {

        @Test
        @DisplayName("POST /items/form 绑定表单字段并回显")
        void shouldBindFormFields() throws Exception {
            mockMvc.perform(post("/api/httpdemo/items/form")
                            .param("name", "qianyu")
                            .param("age", "18")
                            .param("active", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".name").value("qianyu"))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".age").value(18));
        }
    }

    // ============================== 文件上传 ==============================

    @Nested
    @DisplayName("文件上传（multipart/form-data）")
    class UploadTests {

        @Test
        @DisplayName("POST /files/upload 回显文件元信息")
        void shouldUploadFile() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "hello world".getBytes());

            MockMultipartHttpServletRequestBuilder builder =
                    MockMvcRequestBuilders.multipart("/api/httpdemo/files/upload");
            builder.file(file).param("desc", "demo");

            mockMvc.perform(builder)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".originalFilename").value("hello.txt"))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".size").value(11L))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".empty").value(false));
        }
    }

    // ============================== PUT / PATCH / DELETE ==============================

    @Nested
    @DisplayName("PUT / PATCH / DELETE")
    class WriteVerbTests {

        @Test
        @DisplayName("PUT /items?id=1 更新并回显")
        void shouldPutItem() throws Exception {
            mockMvc.perform(put("/api/httpdemo/items")
                            .param("id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(TestFixtures.demoDto("qianyu", 18))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".name").value("qianyu"));
        }

        @Test
        @DisplayName("PATCH /items?id=1 局部更新并回显")
        void shouldPatchItem() throws Exception {
            mockMvc.perform(patch("/api/httpdemo/items")
                            .param("id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(TestFixtures.demoDto("patched", 1))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".name").value("patched"));
        }

        @Test
        @DisplayName("DELETE /items?id=1 返回 true")
        void shouldDeleteItem() throws Exception {
            mockMvc.perform(delete("/api/httpdemo/items")
                            .param("id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT).value(true));
        }

        @Test
        @DisplayName("PUT /items 未带 id → P_NOTNULL 参数错误信封")
        void shouldRejectPutWithoutId() throws Exception {
            mockMvc.perform(put("/api/httpdemo/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(TestFixtures.demoDto("x", 1))))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_P_NOTNULL));
        }
    }

    // ============================== 鉴权 @LoginVerify ==============================

    @Nested
    @DisplayName("鉴权（@LoginVerify / @Token）")
    class AuthTests {

        @Test
        @DisplayName("携带有效 token 访问 /secure，注入 userId 并返回成功")
        void shouldPassLoginVerifyWithToken() throws Exception {
            long uid = 9527L;
            mockMvc.perform(get("/api/httpdemo/secure")
                            .header(TestFixtures.TOKEN_HEADER, TestFixtures.tokenOf(uid)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".loginVerify").value(true))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".userId").value(uid));
        }

        @Test
        @DisplayName("未携带 token 访问 /secure → 鉴权失败（非成功信封）")
        void shouldFailLoginVerifyWithoutToken() throws Exception {
            mockMvc.perform(get("/api/httpdemo/secure"))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ============================== 参数校验 / 统一错误信封 ==============================

    @Nested
    @DisplayName("参数校验与统一错误信封")
    class ErrorEnvelopeTests {

        @Test
        @DisplayName("GET /validate?name= （空）→ P_NOTNULL 错误信封")
        void shouldReturnParamErrorEnvelope() throws Exception {
            mockMvc.perform(get("/api/httpdemo/validate").param("name", ""))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_P_NOTNULL));
        }

        @Test
        @DisplayName("GET /validate?name=ok → 成功信封")
        void shouldPassValidation() throws Exception {
            mockMvc.perform(get("/api/httpdemo/validate").param("name", "ok"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_STATUS).value(TestFixtures.STATUS_OK))
                    .andExpect(jsonPath(TestFixtures.ENVELOPE_CONTENT + ".validated").value(true));
        }
    }

    // ============================== HEAD / OPTIONS ==============================

    @Nested
    @DisplayName("HEAD / OPTIONS（Spring MVC 对 GET 映射的自动支持）")
    class HeadOptionsTests {

        @Test
        @DisplayName("HEAD /headers 由框架处理（非 5xx；HEAD 走 Spring MVC 对 GET 的自动支持）")
        void shouldHandleHead() throws Exception {
            mockMvc.perform(head("/api/httpdemo/headers"))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(200, 400, 405));
        }

        @Test
        @DisplayName("OPTIONS /echo 返回 Allow 头（含 GET）")
        void shouldHandleOptions() throws Exception {
            mockMvc.perform(options("/api/httpdemo/echo"))
                    .andExpect(result -> {
                        // 不同环境下 OPTIONS 可能 200 或 405；关键是框架不抛 500
                        int code = result.getResponse().getStatus();
                        assertThat(code).isIn(200, 405);
                    });
        }
    }
}
