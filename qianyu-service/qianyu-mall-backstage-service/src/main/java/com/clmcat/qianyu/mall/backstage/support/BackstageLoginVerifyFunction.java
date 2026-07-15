package com.clmcat.qianyu.mall.backstage.support;

import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.verify.LoginVerifyFunction;
import com.clmcat.framework.webmvc.verify.TokenInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 运营后台登录校验函数（框架扩展点·方案 A 零侵入）。
 *
 * <p>Controller 类标 {@code @LoginVerify(loginVerify=BackstageLoginVerifyFunction.class, token="X-Admin-Token")}，
 * 由 {@link com.clmcat.framework.webmvc.verify.LoginVerifyService} 按 {@code loginVerify=Class} 解析并
 * {@code applicationContext.getBean(BackstageLoginVerifyFunction.class)} 取实例（{@code @Component} 注入）。
 *
 * <p>verifyLogin: 读 header {@code X-Admin-Token} → Redis {@code admin:session:{token}} →
 * 解析 JSON {@code {adminId,permCodes}} → 滑动续期 2h → 注入 adminId/permCodes 到 request。
 *
 * <p>session key/ATTR 保留 {@code admin:*}（与 {@code t_admin_*} 表语义一致：admin=运营账号，
 * backstage=后台系统）。session 由 login 颁发（P1a-2）。
 */
@Component
public class BackstageLoginVerifyFunction implements LoginVerifyFunction {

    private static final Logger log = LoggerFactory.getLogger(BackstageLoginVerifyFunction.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> SESSION_TYPE = new TypeReference<>() {};

    /** Redis session key 前缀：admin:session:{token} */
    public static final String SESSION_KEY_PREFIX = "admin:session:";

    /** session TTL（2h 滑动续期，决策 3 安全策略采纳缺省值）*/
    public static final long SESSION_TTL_HOURS = 2;

    /** request attribute key：当前 adminId / permCodes（@RequiresPermission 拦截器读取）*/
    public static final String ATTR_ADMIN_ID = "admin:id";
    public static final String ATTR_PERM_CODES = "admin:permCodes";
    public static final String ATTR_USERNAME = "admin:username";

    private final StringRedisTemplate redisTemplate;

    public BackstageLoginVerifyFunction(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean verifyLogin(LoginVerify loginVerify, HttpServletRequest request,
                                HttpServletResponse response, HandlerMethod handlerMethod) {
        String token = request.getHeader(loginVerify.token());
        if (token == null || token.isEmpty()) {
            return false;
        }
        String key = SESSION_KEY_PREFIX + token;
        String sessionJson = redisTemplate.opsForValue().get(key);
        if (sessionJson == null) {
            return false;
        }
        try {
            Map<String, Object> session = MAPPER.readValue(sessionJson, SESSION_TYPE);
            Object adminIdRaw = session.get("adminId");
            if (!(adminIdRaw instanceof Number)) {
                log.warn("admin session adminId 缺失/类型错: {}", token);
                return false;
            }
            Long adminId = ((Number) adminIdRaw).longValue();
            @SuppressWarnings("unchecked")
            List<String> permCodes = (List<String>) session.get("permCodes");

            redisTemplate.expire(key, SESSION_TTL_HOURS, TimeUnit.HOURS);
            // BG-03：同步续期反向索引 admin:session:idx:{adminId}，保证长会话（滑续期）下 disable 仍能按 adminId 吊销。
            redisTemplate.expire(SESSION_KEY_PREFIX + "idx:" + adminId, SESSION_TTL_HOURS + 1, TimeUnit.HOURS);

            request.setAttribute(ATTR_ADMIN_ID, adminId);
            request.setAttribute(ATTR_PERM_CODES, permCodes);
            request.setAttribute(ATTR_USERNAME, session.get("username"));
            return true;
        } catch (Exception e) {
            log.warn("admin session 解析失败 token={}: {}", token, e.getMessage());
            return false;
        }
    }

    @Override
    public TokenInfo parseTokenInfo(HttpServletRequest request) {
        Object adminId = request.getAttribute(ATTR_ADMIN_ID);
        if (adminId instanceof Long) {
            return new BackstageTokenInfo((Long) adminId);
        }
        return new BackstageTokenInfo(null);
    }
}
