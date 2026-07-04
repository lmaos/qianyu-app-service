package com.clmcat.qianyu.mall.test.http;

import java.util.List;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.verify.DefaultTokenCodec;

/**
 * 测试夹具：集中提供 token 生成、示例 DTO 构造与统一信封状态码常量，供 MockMvc 测试复用。
 *
 * <p>状态码取自 {@link ResponseStatus}（成功 {@code status=0}；P 段参数错误如 {@code P_NOTNULL=300002}）。
 */
public final class TestFixtures {

    /** 统一信封：成功 status。 */
    public static final int STATUS_OK = ResponseStatus.OK.getStatus();
    /** P 段参数错误：参数为空。 */
    public static final int STATUS_P_NOTNULL = ResponseStatus.P_NOTNULL.getStatus();

    /** 统一信封字段名。 */
    public static final String ENVELOPE_STATUS = "$.status";
    public static final String ENVELOPE_STATE = "$.state";
    public static final String ENVELOPE_CONTENT = "$.content";
    public static final String ENVELOPE_MESSAGE = "$.message";

    /** 登录态请求头名（框架默认 token 头/参数名）。 */
    public static final String TOKEN_HEADER = "token";

    private TestFixtures() {
    }

    /**
     * 生成一个有效的真实 token（24h 有效），用于在 MockMvc 中通过 {@code @LoginVerify}。
     * <p>走框架真实路径：{@link DefaultTokenCodec#createToken(long, String)}。
     *
     * @param userId 模拟登录用户 ID
     * @return Base64 token，放到 {@value #TOKEN_HEADER} 请求头即可
     */
    public static String tokenOf(long userId) {
        return DefaultTokenCodec.createToken(userId, "127.0.0.1");
    }

    /** 构造一个字段齐全的示例 DTO。 */
    public static HttpDemoController.DemoDto demoDto(String name, Integer age) {
        HttpDemoController.DemoDto dto = new HttpDemoController.DemoDto();
        dto.setName(name);
        dto.setAge(age);
        dto.setActive(Boolean.TRUE);
        dto.setTags(List.of("new", "hot"));
        return dto;
    }
}
