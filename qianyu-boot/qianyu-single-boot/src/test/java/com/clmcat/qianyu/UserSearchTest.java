package com.clmcat.qianyu;

import com.clmcat.qianyu.search.usersearch.service.UserSearchServiceBiz;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户昵称搜索集成测试。
 * <p>
 * 测试三级搜索优先级：完整匹配 → 前缀匹配 → NGram 模糊匹配，以及结果去重。
 * <p>
 * 数据准备直接注入 {@link UserSearchServiceBiz} 调用，搜索验证通过 HTTP 接口测试。
 * <p>
 * 测试前需确保：
 * <ul>
 *   <li>MySQL / Redis 服务已启动</li>
 *   <li>Nacos 配置中心已启动</li>
 *   <li>user_search / user_search_ngram 表已创建</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserSearchTest {

    @LocalServerPort
    private int port;

    @Resource
    private UserSearchServiceBiz userSearchService;

    private final RestTemplate restTemplate = new RestTemplate();

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    // ---------- 辅助方法 ----------

    /** 直接调用 Service 更新昵称索引（不走 HTTP，避免冗余网络开销） */
    private void updateNickname(long userId, String nickname) {
        userSearchService.updateNickname(userId, nickname);
    }

    /** 通过 HTTP 调用昵称搜索 API，返回 content 中的结果列表 */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> search(String keyword) {
        String url = baseUrl() + "/api/user/search/nickname?keyword="
                + URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, null, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("0", String.valueOf(body.get("status")), "search status 应为 0，body=" + body);
        return (List<Map<String, Object>>) body.get("content");
    }

    // ---------- 测试方法 ----------

    /**
     * 准备测试数据。
     * <p>
     * 5 个用户：HelloWorld / HelloKitty / HelloWendy / WorldCup / HellBoy。
     * 选择这些昵称可以覆盖精确、前缀、NGram 三级的交叉验证。
     */
    @Test
    @Order(1)
    void setupTestData() {
        updateNickname(10001L, "HelloWorld");
        updateNickname(10002L, "HelloKitty");
        updateNickname(10003L, "HelloWendy");
        updateNickname(10004L, "WorldCup");
        updateNickname(10005L, "HellBoy");

        System.out.println("✅ 测试数据已写入：5 个用户昵称索引");
    }

    /**
     * 完整匹配：搜索完整昵称，只命中 1 条。
     */
    @Test
    @Order(2)
    void exactMatch() {
        List<Map<String, Object>> results = search("HelloWorld");

        assertFalse(results.isEmpty(), "精确匹配应命中");
        assertEquals("HelloWorld", results.get(0).get("nickname"));
        assertEquals(10001, Long.parseLong(String.valueOf(results.get(0).get("userId"))));

        System.out.println("✅ 精确匹配: userId=" + results.get(0).get("userId") + ", nickname=" + results.get(0).get("nickname"));
    }

    /**
     * 前缀匹配：搜索 "Hello"，应命中 HelloWorld / HelloKitty / HelloWendy（3 条前缀）。
     * HellBoy 也可能出现在结果中（来自 NGram 模糊匹配的补充），不要求排除。
     */
    @Test
    @Order(3)
    void prefixMatch() {
        List<Map<String, Object>> results = search("Hello");

        // 应至少包含 3 个 Hello 前缀的用户
        assertTrue(results.size() >= 3, "前缀匹配应至少命中 3 条，实际=" + results.size());

        // 验证前缀命中的用户都在结果中
        Set<String> nicknames = new HashSet<>();
        for (Map<String, Object> r : results) {
            nicknames.add((String) r.get("nickname"));
        }
        assertTrue(nicknames.contains("HelloWorld"), "应包含 HelloWorld");
        assertTrue(nicknames.contains("HelloKitty"), "应包含 HelloKitty");
        assertTrue(nicknames.contains("HelloWendy"), "应包含 HelloWendy");

        System.out.println("✅ 前缀匹配: 命中 " + results.size() + " 条 → " + nicknames);
    }

    /**
     * NGram 模糊匹配：故意拼错 "HlloWrld"（少了 e 和 o），
     * bigram 切分后仍能匹配到 HelloWorld。
     */
    @Test
    @Order(4)
    void ngramFuzzyMatch() {
        // "HlloWrld" ngram(n=2): {Hl, ll, lo, oW, Wr, rl, ld} (7 tokens)
        // "HelloWorld" ngram(n=2): {He, el, ll, lo, oW, Wo, or, rl, ld} (9 tokens)
        // 交集: ll, lo, oW, rl, ld → 5 个匹配，阈值 60% → ceil(7*0.6)=5 → 能命中
        List<Map<String, Object>> results = search("HlloWrld");

        assertFalse(results.isEmpty(), "NGram 模糊匹配应能命中至少 1 条");

        // 验证命中的昵称确实包含搜索词的大部分 bigram
        boolean foundHelloWorld = false;
        for (Map<String, Object> r : results) {
            if ("HelloWorld".equals(r.get("nickname"))) {
                foundHelloWorld = true;
                break;
            }
        }
        assertTrue(foundHelloWorld, "NGram 应能命中 HelloWorld");

        System.out.println("✅ NGram 模糊匹配: 命中 " + results.size() + " 条");
    }

    /**
     * 去重验证：同一个用户不应在不同优先级重复出现。
     */
    @Test
    @Order(5)
    void noDuplicateResults() {
        List<Map<String, Object>> results = search("Hello");

        Set<Long> seenIds = new HashSet<>();
        for (Map<String, Object> r : results) {
            Long userId = Long.parseLong(String.valueOf(r.get("userId")));
            assertFalse(seenIds.contains(userId),
                    "搜索结果不应包含重复 userId: " + userId);
            seenIds.add(userId);
        }

        System.out.println("✅ 去重验证通过: " + results.size() + " 条结果无重复");
    }

    /**
     * 搜索不存在的昵称，应返回空列表。
     */
    @Test
    @Order(6)
    void noMatch() {
        List<Map<String, Object>> results = search("XyzzyNotExist999");

        assertTrue(results.isEmpty(), "不存在的昵称应返回空列表");

        System.out.println("✅ 无匹配: 返回空列表");
    }

    /**
     * 空关键词应返回空列表。
     */
    @Test
    @Order(7)
    void emptyKeyword() {
        List<Map<String, Object>> results = search("");

        assertTrue(results.isEmpty(), "空关键词应返回空列表");

        System.out.println("✅ 空关键词: 返回空列表");
    }

    /**
     * 修改昵称后再搜索，新昵称可命中，旧昵称不再命中。
     */
    @Test
    @Order(8)
    void updateNicknameThenSearch() {
        // 把 HellBoy 改成 "DarkKnight"
        updateNickname(10005L, "DarkKnight");

        // 旧昵称搜不到
        List<Map<String, Object>> oldResults = search("HellBoy");
        boolean containsHellBoy = oldResults.stream()
                .anyMatch(r -> "HellBoy".equals(r.get("nickname")));
        assertFalse(containsHellBoy, "旧昵称 HellBoy 不应再被搜到");

        // 新昵称精确命中
        List<Map<String, Object>> newResults = search("DarkKnight");
        assertFalse(newResults.isEmpty(), "新昵称 DarkKnight 应能搜到");
        assertEquals("DarkKnight", newResults.get(0).get("nickname"));
        assertEquals(10005, Long.parseLong(String.valueOf(newResults.get(0).get("userId"))));

        System.out.println("✅ 更新昵称验证: HellBoy → DarkKnight，旧名不可搜，新名可搜");
    }
}
