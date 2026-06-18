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
 * 个人中心接口集成测试。
 * <p>
 * 测试前需确保：
 * <ul>
 *   <li>MySQL / Redis 服务已启动</li>
 *   <li>Nacos 配置中心已启动（local 环境 namespace=qianyu-local）</li>
 * </ul>
 * <p>
 * 使用手机号 13800138000 验证码 123456 登录获取 token。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonalCenterTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String token;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * 手机号登录获取 token。
     */
    @Test
    @Order(1)
    void login() {
        // 构造 JSON 请求体，phone 必须带国家码：+86-13800138000
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

        System.out.println("✅ 登录成功，token=" + (token.length() > 20 ? token.substring(0, 20) + "..." : token));
    }

    /**
     * 获取个人中心整体数据。
     */
    @Test
    @Order(2)
    void getPersonalCenter() {
        assertNotNull(token, "请先执行 login() 获取 token");

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/app/personal/center",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "status 应为 0");

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);

        // 验证 userProfile
        Map<String, Object> userProfile = (Map<String, Object>) content.get("userProfile");
        assertNotNull(userProfile, "userProfile 不应为空");
        assertNotNull(userProfile.get("userNo"), "userNo 不应为空");
        assertNotNull(userProfile.get("nickname"), "nickname 不应为空");

        // 验证 userStats
        Map<String, Object> userStats = (Map<String, Object>) content.get("userStats");
        assertNotNull(userStats, "userStats 不应为空");
        assertNotNull(userStats.get("likeCount"), "likeCount 不应为空");
        assertNotNull(userStats.get("followCount"), "followCount 不应为空");
        assertNotNull(userStats.get("fansCount"), "fansCount 不应为空");
        assertNotNull(userStats.get("visitorCount"), "visitorCount 不应为空");

        // 验证 shortcuts
        Object shortcuts = content.get("shortcuts");
        assertNotNull(shortcuts, "shortcuts 不应为空");

        System.out.println("✅ 个人中心数据：userNo=" + userProfile.get("userNo") + ", nickname=" + userProfile.get("nickname") + ", stats=" + userStats);
    }

    /**
     * 分页查询动态列表。
     */
    @Test
    @Order(3)
    void getMomentContents() {
        assertNotNull(token, "请先执行 login() 获取 token");

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/app/personal/center/contents?tab=moment&cursor=0&limit=10",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")));

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        assertNotNull(content.get("items"));
        assertNotNull(content.get("hasMore"));
        assertNotNull(content.get("nextCursor"));

        System.out.println("✅ 动态列表 hasMore=" + content.get("hasMore") + " itemsCount=" + ((java.util.List) content.get("items")).size());
    }

    /**
     * 分页查询作品列表（纯视频）。
     */
    @Test
    @Order(4)
    void getWorkContents() {
        assertNotNull(token, "请先执行 login() 获取 token");

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/app/personal/center/contents?tab=work&cursor=0&limit=10",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")));

        System.out.println("✅ 作品列表（纯视频）请求成功");
    }

    /**
     * 分页查询喜欢列表。
     */
    @Test
    @Order(5)
    void getLikeContents() {
        assertNotNull(token, "请先执行 login() 获取 token");

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/app/personal/center/contents?tab=like&cursor=0&limit=10",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")));

        System.out.println("✅ 喜欢列表请求成功");
    }

    /**
     * 分页查询历史列表。
     */
    @Test
    @Order(6)
    void getHistoryContents() {
        assertNotNull(token, "请先执行 login() 获取 token");

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/app/personal/center/contents?tab=history&cursor=0&limit=10",
                HttpMethod.GET,
                request,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")));

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        // 历史功能未上线，应返回空列表
        assertEquals(false, content.get("hasMore"));

        System.out.println("✅ 历史列表（空）请求成功");
    }
}
