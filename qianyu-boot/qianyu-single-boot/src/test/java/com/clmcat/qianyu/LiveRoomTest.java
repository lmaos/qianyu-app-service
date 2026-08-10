package com.clmcat.qianyu;

import com.clmcat.qianyu.live.room.service.LiveRoomServiceBiz;
import jakarta.annotation.Resource;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 直播间集成测试。
 * <p>
 * 覆盖创建、开播、关播、详情、列表、我的直播间、更新编号等全部核心方法。
 * 需登录的接口通过手机号 +8613800138000 验证码 123456 登录获取 token。
 * <p>
 * 测试前需确保：
 * <ul>
 *   <li>MySQL / Redis / Nacos 已启动</li>
 *   <li>live_room / live_room_count 表已创建</li>
 * </ul>
 *
 * @author ark-home
 * @date 2026-07-08
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LiveRoomTest {

    @LocalServerPort
    private int port;

    @Resource
    private LiveRoomServiceBiz liveRoomServiceBiz;

    private final RestTemplate restTemplate = new RestTemplate();

    private String token;
    private Long testRoomNo;
    private Long secondRoomNo;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    // ---------- 辅助方法 ----------

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.set("token", token);
        }
        return headers;
    }

    /** 发起 GET，期望 200 OK，返回解析的 body */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> get(String path) {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + path, HttpMethod.GET, request, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        return body;
    }

    /** 发起 POST，期望 200 OK，返回解析的 body */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> post(String path) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + path, HttpMethod.POST, request, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        return body;
    }

    /** 发起 POST，期望业务错误（400），从异常中提取 body */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> postExpectError(String path) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(baseUrl() + path, HttpMethod.POST, request, Map.class);
            fail("应抛出 HttpClientErrorException，但请求成功了: " + path);
            return null;
        } catch (HttpClientErrorException e) {
            assertTrue(e.getStatusCode().is4xxClientError(),
                    "期望 4xx 错误，实际: " + e.getStatusCode());
            // 从 response body 解析 JSON
            Map<String, Object> body = e.getResponseBodyAs(Map.class);
            assertNotNull(body);
            return body;
        }
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? Long.parseLong(String.valueOf(val)) : null;
    }

    private int getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return Integer.parseInt(String.valueOf(val));
    }

    // ---------- 测试方法 ----------

    /**
     * 1. 登录获取 token。
     */
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
                HttpMethod.POST, request, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "登录失败: " + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        token = (String) content.get("token");
        assertNotNull(token, "token 不应为空");

        System.out.println("✅ 登录成功");
    }

    /**
     * 2. 创建直播间。
     */
    @Test
    @Order(2)
    void createRoom() {
        String url = "/api/live/room/create?title=测试直播间&coverImage=https://img.example.com/cover.jpg";
        Map<String, Object> body = post(url);
        assertEquals("0", String.valueOf(body.get("status")), "创建失败: " + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        testRoomNo = getLong(content, "roomNo");
        assertNotNull(testRoomNo);
        assertTrue(testRoomNo > 0);
        assertEquals("测试直播间", content.get("title"));
        assertEquals(0, getInt(content, "status"));
        assertEquals(0L, getLong(content, "viewerCount"));

        System.out.println("✅ 创建直播间: roomNo=" + testRoomNo);
    }

    /**
     * 3. 创建第二个直播间。
     */
    @Test
    @Order(3)
    void createSecondRoom() {
        String url = "/api/live/room/create?title=第二个直播间";
        Map<String, Object> body = post(url);
        assertEquals("0", String.valueOf(body.get("status")));

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        secondRoomNo = getLong(content, "roomNo");
        assertNotNull(secondRoomNo);

        System.out.println("✅ 创建第二直播间: roomNo=" + secondRoomNo);
    }

    /**
     * 4. 查询我的直播间（取最新一条 = 第二个）。
     */
    @Test
    @Order(4)
    void getMyLiveRoom() {
        Map<String, Object> body = get("/api/live/room/my");
        assertEquals("0", String.valueOf(body.get("status")));

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        // 最新创建的应该是 secondRoomNo
        assertEquals(secondRoomNo, getLong(content, "roomNo"),
                "my 应返回最近创建的直播间");

        System.out.println("✅ 我的直播间: roomNo=" + content.get("roomNo"));
    }

    /**
     * 5. 开播（testRoomNo，返回推流地址）。
     */
    @Test
    @Order(5)
    void startLive() {
        String url = "/api/live/room/start?roomNo=" + testRoomNo;
        Map<String, Object> body = post(url);
        assertEquals("0", String.valueOf(body.get("status")), "开播失败: " + body);

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        assertEquals(testRoomNo, getLong(content, "roomNo"));
        assertNotNull(content.get("pushUrl"), "pushUrl 不应为 null");
        assertNotNull(content.get("pushType"), "pushType 不应为 null");
        assertTrue(getLong(content, "startTime") > 0, "startTime 应 > 0");

        System.out.println("✅ 开播成功: pushUrl=" + content.get("pushUrl") + ", type=" + content.get("pushType"));
    }

    /**
     * 6. 重复开播应失败（已是直播中）。
     */
    @Test
    @Order(6)
    void startLiveTwiceShouldFail() {
        Map<String, Object> body = postExpectError("/api/live/room/start?roomNo=" + testRoomNo);
        assertNotEquals("0", String.valueOf(body.get("status")),
                "重复开播应失败: " + body);

        System.out.println("✅ 重复开播正确拒绝: " + body.get("message"));
    }

    /**
     * 7. 查询"直播中"列表（testRoomNo 应在列表中）。
     */
    @Test
    @Order(7)
    void getLiveRoomList() {
        Map<String, Object> body = get("/api/live/room/list?nextNo=0&limit=20");
        assertEquals("0", String.valueOf(body.get("status")));

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);

        List<Map<String, Object>> rooms = (List<Map<String, Object>>) content.get("rooms");
        assertNotNull(rooms);
        assertFalse(rooms.isEmpty(), "直播中列表不应为空");

        boolean found = rooms.stream().anyMatch(r -> testRoomNo.equals(getLong(r, "roomNo")));
        assertTrue(found, "列表应包含 testRoomNo");

        System.out.println("✅ 直播中列表: " + rooms.size() + " 个, hasMore=" + content.get("hasMore"));
    }

    /**
     * 8. 查询直播间详情（含计数器）。
     */
    @Test
    @Order(8)
    void getRoomInfo() {
        Map<String, Object> body = get("/api/live/room/info?roomNo=" + testRoomNo);
        assertEquals("0", String.valueOf(body.get("status")));

        Map<String, Object> content = (Map<String, Object>) body.get("content");
        assertNotNull(content);
        assertEquals(testRoomNo, getLong(content, "roomNo"));
        assertEquals(1, getInt(content, "status"), "状态应为直播中(1)");
        assertTrue(getLong(content, "startTime") > 0);

        System.out.println("✅ 详情: status=" + content.get("status")
                + ", viewerCount=" + content.get("viewerCount")
                + ", likeCount=" + content.get("likeCount"));
    }

    /**
     * 9. 查询不存在的直播间返回 null。
     */
    @Test
    @Order(9)
    void getRoomInfoNotFound() {
        Map<String, Object> body = get("/api/live/room/info?roomNo=999999999");
        assertEquals("0", String.valueOf(body.get("status")));
        assertNull(body.get("content"), "不存在应返回 null");

        System.out.println("✅ 查询不存在直播间返回 null");
    }

    /**
     * 10. 关播。
     */
    @Test
    @Order(10)
    void closeLive() {
        String url = "/api/live/room/close?roomNo=" + testRoomNo;
        Map<String, Object> body = post(url);
        assertEquals("0", String.valueOf(body.get("status")), "关播失败: " + body);

        // 验证状态已变为已结束
        Map<String, Object> infoBody = get("/api/live/room/info?roomNo=" + testRoomNo);
        Map<String, Object> info = (Map<String, Object>) infoBody.get("content");
        assertEquals(2, getInt(info, "status"));
        assertTrue(getLong(info, "endTime") > 0);

        System.out.println("✅ 关播成功: endTime=" + info.get("endTime"));
    }

    /**
     * 11. 关播后不可再次关播。
     */
    @Test
    @Order(11)
    void closeLiveTwiceShouldFail() {
        Map<String, Object> body = postExpectError("/api/live/room/close?roomNo=" + testRoomNo);
        assertNotEquals("0", String.valueOf(body.get("status")),
                "重复关播应失败: " + body);

        System.out.println("✅ 重复关播正确拒绝: " + body.get("message"));
    }

    /**
     * 12. 更新直播间编号（待开播状态允许）。
     */
    @Test
    @Order(12)
    void updateRoomNo() {
        long newRoomNo = secondRoomNo + 10000;
        String url = "/api/live/room/update_room_no?roomNo=" + secondRoomNo + "&newRoomNo=" + newRoomNo;
        Map<String, Object> body = post(url);
        assertEquals("0", String.valueOf(body.get("status")), "更新编号失败: " + body);

        // 新编号能查到
        Map<String, Object> infoBody = get("/api/live/room/info?roomNo=" + newRoomNo);
        Map<String, Object> info = (Map<String, Object>) infoBody.get("content");
        assertNotNull(info, "新 roomNo 应能查到");
        assertEquals(newRoomNo, getLong(info, "roomNo"));

        secondRoomNo = newRoomNo;

        System.out.println("✅ 更新编号成功: newRoomNo=" + newRoomNo);
    }

    /**
     * 13. 开播后不可改编号。
     */
    @Test
    @Order(13)
    void updateRoomNoOnLiveShouldFail() {
        // 开播
        Map<String, Object> startBody = post("/api/live/room/start?roomNo=" + secondRoomNo);
        assertEquals("0", String.valueOf(startBody.get("status")), "开播失败: " + startBody);

        // 尝试改编号
        long newRoomNo = secondRoomNo + 20000;
        Map<String, Object> body = postExpectError(
                "/api/live/room/update_room_no?roomNo=" + secondRoomNo + "&newRoomNo=" + newRoomNo);
        assertNotEquals("0", String.valueOf(body.get("status")),
                "直播中改编号应被拒绝: " + body);

        System.out.println("✅ 直播中改编号正确拒绝: " + body.get("message"));
    }
}
