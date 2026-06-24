package com.clmcat.qianyu;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 按作者+类型查询动态接口测试。
 * <p>
 * 测试前需确保：
 * <ul>
 *   <li>MySQL / Redis 服务已启动</li>
 *   <li>Nacos 配置中心已启动（local 环境 namespace=qianyu-local）</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MomentAuthorTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String token;
    private Long authorId;
    private Long textMomentId;
    private Long imageMomentId;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    // ==================== 登录 ====================

    @Test
    @Order(1)
    void login() {
        String jsonBody = """
                {"phone":"+86-13800138000","code":"123456"}
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/user/login/phone",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "登录 status 应为 0，实际 body=" + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        token = (String) content.get("token");
        assertNotNull(token, "登录 token 不应为空");

        // 通过 /api/user/value 获取当前登录用户的 userId
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("token", token);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(
                baseUrl() + "/api/user/value",
                HttpMethod.GET,
                userRequest,
                Map.class
        );
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        Map<String, Object> userBody = userResponse.getBody();
        assertNotNull(userBody);
        // userValue 格式："user:2495058814603264"
        String userValue = String.valueOf(userBody.get("content"));
        assertNotNull(userValue);
        authorId = Long.valueOf(userValue.replace("user:", ""));
        assertNotNull(authorId, "authorId 不应为空");

        System.out.println("✅ 登录成功，authorId=" + authorId);
    }

    // ==================== 发布动态 ====================

    @Test
    @Order(2)
    void publishTextMoment() {
        assertNotNull(token, "请先执行 login()");

        String jsonBody = """
                {
                  "content": {
                    "type": "text",
                    "text": {
                      "text": "纯文本动态-测试",
                      "atIds": []
                    }
                  }
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("token", token);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/moment/publish",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "发布 text 动态失败: " + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        textMomentId = Long.valueOf(String.valueOf(content.get("momentId")));
        assertNotNull(textMomentId);
        assertEquals("text", ((Map<String, Object>) content.get("content")).get("type"));

        System.out.println("✅ 发布 text 动态成功，momentId=" + textMomentId);
    }

    @Test
    @Order(3)
    void publishImageMoment() {
        assertNotNull(token, "请先执行 login()");

        String jsonBody = """
                {
                  "content": {
                    "type": "image",
                    "text": {
                      "text": "图文动态-测试",
                      "atIds": []
                    },
                    "image": [
                      {
                        "imageId": "img_test_001",
                        "imageUrl": "https://cdn.example.com/test.jpg",
                        "width": 1080,
                        "height": 1080
                      }
                    ]
                  }
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("token", token);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/moment/publish",
                HttpMethod.POST,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "发布 image 动态失败: " + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        imageMomentId = Long.valueOf(String.valueOf(content.get("momentId")));
        assertNotNull(imageMomentId);
        assertEquals("image", ((Map<String, Object>) content.get("content")).get("type"));

        System.out.println("✅ 发布 image 动态成功，momentId=" + imageMomentId);
    }

    // ==================== 按作者查询全部动态 ====================

    @Test
    @Order(4)
    void getAuthorListAll() {
        assertNotNull(token);
        assertNotNull(authorId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/moment/author/list?authorId=" + authorId + "&limit=20",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "查作者全部动态失败: " + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        assertEquals(authorId, Long.valueOf(String.valueOf(content.get("authorId"))));
        assertNotNull(content.get("hasMore"));
        assertNotNull(content.get("nextMomentId"));

        List<Map<String, Object>> datas = (List<Map<String, Object>>) content.get("datas");
        assertNotNull(datas, "datas 不应为空");
        assertTrue(datas.size() >= 2, "至少应该有 2 条动态（text + image）");

        // 验证每条都包含 nickname 和 avatar
        for (Map<String, Object> item : datas) {
            assertNotNull(item.get("momentId"));
            assertNotNull(item.get("authorId"));
            assertNotNull(item.get("content"));
            // nickname/avatar 由 UserApi 批量填充
            System.out.println("  momentId=" + item.get("momentId") + " nickname=" + item.get("nickname"));
        }

        System.out.println("✅ 按作者查询全部动态成功，共 " + datas.size() + " 条");
    }

    // ==================== 按作者+类型查询 ====================

    @Test
    @Order(5)
    void getAuthorListByTypeImage() {
        assertNotNull(token);
        assertNotNull(authorId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/moment/author/list/type?authorId=" + authorId + "&momentType=image&limit=20",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "查 image 类型动态失败: " + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        assertEquals(authorId, Long.valueOf(String.valueOf(content.get("authorId"))));

        List<Map<String, Object>> datas = (List<Map<String, Object>>) content.get("datas");
        assertNotNull(datas, "datas 不应为空");
        assertTrue(datas.size() >= 1, "至少应该有 1 条 image 动态");

        // 所有返回的动态类型都必须是 image
        for (Map<String, Object> item : datas) {
            Map<String, Object> momentContent = (Map<String, Object>) item.get("content");
            assertNotNull(momentContent);
            assertEquals("image", momentContent.get("type"),
                    "类型过滤失效：期望 image，实际 " + momentContent.get("type"));
        }

        System.out.println("✅ 按作者+类型(image)查询成功，共 " + datas.size() + " 条");
    }

    @Test
    @Order(6)
    void getAuthorListByTypeText() {
        assertNotNull(token);
        assertNotNull(authorId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/moment/author/list/type?authorId=" + authorId + "&momentType=text&limit=20",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "查 text 类型动态失败: " + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);

        List<Map<String, Object>> datas = (List<Map<String, Object>>) content.get("datas");
        assertNotNull(datas, "datas 不应为空");
        assertTrue(datas.size() >= 1, "至少应该有 1 条 text 动态");

        // 所有返回的动态类型都必须是 text
        for (Map<String, Object> item : datas) {
            Map<String, Object> momentContent = (Map<String, Object>) item.get("content");
            assertNotNull(momentContent);
            assertEquals("text", momentContent.get("type"),
                    "类型过滤失效：期望 text，实际 " + momentContent.get("type"));
        }

        System.out.println("✅ 按作者+类型(text)查询成功，共 " + datas.size() + " 条");
    }

    @Test
    @Order(7)
    void getAuthorListByTypeVideoNoData() {
        assertNotNull(token);
        assertNotNull(authorId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 没有发布过 video 类型，应返回空列表
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/moment/author/list/type?authorId=" + authorId + "&momentType=video&limit=20",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "查 video 类型应正常返回: " + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        assertEquals(false, content.get("hasMore"), "没有 video 时应 hasMore=false");

        List<Map<String, Object>> datas = (List<Map<String, Object>>) content.get("datas");
        assertNotNull(datas, "datas 可以为空列表但不是 null");
        assertEquals(0, datas.size(), "没有发布过 video，列表应为空");

        System.out.println("✅ 按作者+类型(video)查询为空，符合预期");
    }

    // ==================== 参数校验 ====================

    @Test
    @Order(8)
    void getAuthorListByTypeInvalidType() {
        assertNotNull(token);
        assertNotNull(authorId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 非法类型值
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/moment/author/list/type?authorId=" + authorId + "&momentType=invalid&limit=20",
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        // 应该返回 422 业务错误（非法类型）
        String status = String.valueOf(body.get("status"));
        assertNotEquals("0", status, "非法类型应返回非 0 status，实际 body=" + body);

        System.out.println("✅ 非法类型参数校验通过，status=" + status + " message=" + body.get("message"));
    }

    @Test
    @Order(9)
    void getAuthorListByTypeMissingAuthorId() {
        assertNotNull(token);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 缺少 authorId
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/moment/author/list/type?momentType=image&limit=20",
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        String status = String.valueOf(body.get("status"));
        assertNotEquals("0", status, "缺少 authorId 应返回错误，实际 body=" + body);

        System.out.println("✅ 缺少 authorId 参数校验通过");
    }

    @Test
    @Order(10)
    void getAuthorListByTypeMissingType() {
        assertNotNull(token);
        assertNotNull(authorId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 缺少 momentType
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/moment/author/list/type?authorId=" + authorId + "&limit=20",
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        String status = String.valueOf(body.get("status"));
        assertNotEquals("0", status, "缺少 momentType 应返回错误，实际 body=" + body);

        System.out.println("✅ 缺少 momentType 参数校验通过");
    }

    // ==================== 游标分页 ====================

    @Test
    @Order(11)
    void getAuthorListByTypePagination() {
        assertNotNull(token);
        assertNotNull(authorId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 第一页：limit=1
        ResponseEntity<Map> page1 = restTemplate.exchange(
                baseUrl() + "/api/social/moment/author/list/type?authorId=" + authorId + "&momentType=text&limit=1",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, page1.getStatusCode());
        Map<String, Object> body1 = page1.getBody();
        assertNotNull(body1);
        assertEquals("0", String.valueOf(body1.get("status")));

        Map<String, Object> content1 = (Map<String, Object>) body1.get("content");
        List<Map<String, Object>> datas1 = (List<Map<String, Object>>) content1.get("datas");
        assertTrue(datas1.size() <= 1, "limit=1 时最多 1 条");

        if (Boolean.TRUE.equals(content1.get("hasMore")) && !datas1.isEmpty()) {
            long nextMomentId = Long.parseLong(String.valueOf(content1.get("nextMomentId")));
            assertTrue(nextMomentId > 0, "hasMore=true 时 nextMomentId 应大于 0");

            // 第二页
            ResponseEntity<Map> page2 = restTemplate.exchange(
                    baseUrl() + "/api/social/moment/author/list/type?authorId=" + authorId
                            + "&momentType=text&limit=1&momentId=" + nextMomentId,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            assertEquals(HttpStatus.OK, page2.getStatusCode());
            Map<String, Object> body2 = page2.getBody();
            assertEquals("0", String.valueOf(body2.get("status")));

            Map<String, Object> content2 = (Map<String, Object>) body2.get("content");
            List<Map<String, Object>> datas2 = (List<Map<String, Object>>) content2.get("datas");
            if (!datas2.isEmpty()) {
                long page2MomentId = Long.parseLong(String.valueOf(datas2.get(0).get("momentId")));
                assertTrue(page2MomentId < nextMomentId,
                        "第二页的 momentId (" + page2MomentId + ") 应小于游标 (" + nextMomentId + ")");
                System.out.println("✅ 游标分页正常：第1页 momentId > 游标 > 第2页 momentId");
            }
        }

        System.out.println("✅ 游标分页测试完成");
    }
}
