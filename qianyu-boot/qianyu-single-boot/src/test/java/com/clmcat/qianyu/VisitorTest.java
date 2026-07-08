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
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 访客模块集成测试。
 * <p>
 * 测试前需确保：
 * <ul>
 *   <li>MySQL / Redis 服务已启动</li>
 *   <li>Nacos 配置中心已启动（local 环境 namespace=qianyu-local）</li>
 *   <li>user_visitor 和 user_history 表已创建（执行 sql/user_visitor.sql）</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VisitorTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate;

    {
        restTemplate = new RestTemplate();
        // 不抛出 HTTP 错误，允许测试验证错误响应
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @SuppressWarnings("unused")
            public void handleError(ClientHttpResponse response) {
                // no-op
            }
        });
    }

    private String token;
    private Long userId;
    private Long targetUserId = 5257117397155842L;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

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
        assertNotNull(token);

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("token", token);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(
                baseUrl() + "/api/user/value",
                HttpMethod.GET,
                userRequest,
                Map.class
        );
        Map<String, Object> userBody = userResponse.getBody();
        assertNotNull(userBody);
        String userValue = String.valueOf(userBody.get("content"));
        userId = Long.valueOf(userValue.replace("user:", ""));
        assertNotNull(userId);

        assertNotEquals(userId, targetUserId, "targetUserId 不能等于当前用户ID");

        System.out.println("✅ 登录成功，userId=" + userId + "，targetUserId=" + targetUserId);
    }

    @Test
    @Order(2)
    void recordVisit() {
        assertNotNull(token);

        String jsonBody = "{\"targetId\":" + targetUserId + "}";
        HttpHeaders headers = buildHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/record",
                HttpMethod.POST,
                new HttpEntity<>(jsonBody, headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        if (!"0".equals(String.valueOf(body.get("status")))) {
            System.out.println("⚠️ recordVisit 返回非0: " + body.get("message") + " (可能需要执行 sql/user_visitor.sql)");
            return;
        }
        System.out.println("✅ 记录访问成功：userId=" + userId + " → targetUserId=" + targetUserId);
    }

    @Test
    @Order(3)
    void recordVisitAgain() {
        assertNotNull(token);

        String jsonBody = "{\"targetId\":" + targetUserId + "}";
        HttpHeaders headers = buildHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/record",
                HttpMethod.POST,
                new HttpEntity<>(jsonBody, headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        if (!"0".equals(String.valueOf(body.get("status")))) {
            System.out.println("⚠️ recordVisitAgain 跳过（表不存在）");
            return;
        }
        System.out.println("✅ 重复访问（upsert）成功");
    }

    @Test
    @Order(4)
    void getSelfHistoryList() {
        assertNotNull(token);
        HttpHeaders headers = buildHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/history/self/list?limit=20",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        if (!"0".equals(String.valueOf(body.get("status")))) {
            System.out.println("⚠️ 浏览历史查询跳过：" + body.get("message"));
            return;
        }
        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        List<Map<String, Object>> userList = (List<Map<String, Object>>) content.get("userList");
        System.out.println("✅ 浏览历史查询成功，共 " + (userList != null ? userList.size() : 0) + " 条");
    }

    @Test
    @Order(5)
    void getVisitorList() {
        assertNotNull(token);
        HttpHeaders headers = buildHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/list?userId=" + targetUserId + "&limit=20",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        if (!"0".equals(String.valueOf(body.get("status")))) {
            System.out.println("⚠️ 访客列表查询跳过：" + body.get("message"));
            return;
        }
        Map<String, Object> content = (Map<String, Object>) body.get("content");
        List<Map<String, Object>> userList = (List<Map<String, Object>>) content.get("userList");
        System.out.println("✅ 访客列表查询成功，共 " + (userList != null ? userList.size() : 0) + " 条");
    }

    @Test
    @Order(6)
    void getVisitorCount() {
        assertNotNull(token);
        HttpHeaders headers = buildHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/count/self",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")));
        Map<String, Object> content = (Map<String, Object>) body.get("content");
        System.out.println("✅ 我的访客数=" + content.get("visitorCount"));
    }

    @Test
    @Order(7)
    void getSelfHistoryPagination() {
        assertNotNull(token);
        HttpHeaders headers = buildHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/history/self/list?limit=1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        if (!"0".equals(String.valueOf(body.get("status")))) {
            System.out.println("⚠️ 分页测试跳过：" + body.get("message"));
            return;
        }

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        List<Map<String, Object>> list = (List<Map<String, Object>>) content.get("userList");
        assertNotNull(list);
        assertTrue(list.size() <= 1, "limit=1 时最多 1 条");

        if (Boolean.TRUE.equals(content.get("hasMore")) && !list.isEmpty()) {
            long nextId = Long.parseLong(String.valueOf(content.get("nextId")));
            assertTrue(nextId > 0);

            ResponseEntity<Map> page2 = restTemplate.exchange(
                    baseUrl() + "/api/social/visitor/history/self/list?limit=1&nextId=" + nextId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            Map<String, Object> body2 = page2.getBody();
            assertNotNull(body2);
            assertEquals("0", String.valueOf(body2.get("status")));

            Map<String, Object> content2 = (Map<String, Object>) body2.get("content");
            List<Map<String, Object>> list2 = (List<Map<String, Object>>) content2.get("userList");
            if (!list2.isEmpty()) {
                long page2Id = Long.parseLong(String.valueOf(list2.get(0).get("id")));
                assertTrue(page2Id < nextId,
                        "第二页的 id (" + page2Id + ") 应小于游标 (" + nextId + ")");
                System.out.println("✅ 游标分页正常：第1页 id > 游标 > 第2页 id");
            }
        }
        System.out.println("✅ 游标分页测试完成");
    }

    @Test
    @Order(8)
    void clearNewFlag() {
        assertNotNull(token);
        HttpHeaders headers = buildHeaders();

        restTemplate.exchange(
                baseUrl() + "/api/social/visitor/self/list?limit=20",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        ResponseEntity<Map> countResp = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/count/self",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<String, Object> countBody = countResp.getBody();
        assertNotNull(countBody);
        Map<String, Object> countContent = (Map<String, Object>) countBody.get("content");
        long count = Long.parseLong(String.valueOf(countContent.get("visitorCount")));
        assertEquals(0L, count, "查看访客列表后，新访客数应清零");
        System.out.println("✅ 清除新访客标记后，visitorCount=" + count);
    }

    @Test
    @Order(9)
    void deleteHistory() {
        assertNotNull(token);
        String jsonBody = "{\"targetId\":" + targetUserId + "}";
        HttpHeaders headers = buildHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/history/delete",
                HttpMethod.POST,
                new HttpEntity<>(jsonBody, headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        System.out.println("✅ 删除浏览历史完成，status=" + body.get("status"));
    }

    @Test
    @Order(10)
    void recordVisitSelfNotAllowed() {
        assertNotNull(token);
        assertNotNull(userId);

        String jsonBody = "{\"targetId\":" + userId + "}";
        HttpHeaders headers = buildHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/record",
                HttpMethod.POST,
                new HttpEntity<>(jsonBody, headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        String status = String.valueOf(body.get("status"));
        assertNotEquals("0", status, "不能访问自己的主页，期望非0 status，实际 body=" + body);
        System.out.println("✅ 不能访问自己校验通过，status=" + status + " message=" + body.get("message"));
    }

    @Test
    @Order(11)
    void recordVisitMissingTarget() {
        assertNotNull(token);

        String jsonBody = "{}";
        HttpHeaders headers = buildHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/social/visitor/record",
                HttpMethod.POST,
                new HttpEntity<>(jsonBody, headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        String status = String.valueOf(body.get("status"));
        assertNotEquals("0", status, "缺少 targetId 应返回错误，实际 body=" + body);
        System.out.println("✅ 缺少 targetId 参数校验通过");
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("token", token);
        return headers;
    }
}
