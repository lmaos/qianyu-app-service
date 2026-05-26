package com.clmcat.qianyu.im.provider;

import com.clmcat.qianyu.im.config.EasemobImConfig;
import com.clmcat.qianyu.im.model.enums.Channel;
import com.clmcat.qianyu.im.model.enums.ChatType;
import com.clmcat.qianyu.im.model.body.MessageBody;
import com.clmcat.qianyu.im.util.ImHttpClient;
import com.clmcat.qianyu.im.util.SignatureUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 环信 IM Provider
 *
 * REST API 鉴权: Bearer Token (OAuth2 client_credentials)
 * 动态 Token 生成: SHA256 本地计算，零网络开销
 */
@Slf4j
@Service
public class EasemobProvider implements IMProvider {

    @Resource
    private EasemobImConfig config;

    @Resource
    private ImHttpClient httpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 缓存的 App Token */
    private volatile String cachedAppToken;
    /** App Token 过期时间（毫秒时间戳） */
    private volatile long appTokenExpireAt;

    @Override
    public String sendMessage(MessageBody body) {
        try {
            String appToken = getAppToken();

            String url;
            if (body.getChatType() != null && body.getChatType() == ChatType.GROUP.getCode()) {
                url = String.format("%s/%s/%s/messages/chatgroups", config.getHost(), config.getOrgName(), config.getAppName());
            } else {
                url = String.format("%s/%s/%s/messages/users", config.getHost(), config.getOrgName(), config.getAppName());
            }

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("from", body.getSender());
            requestBody.putArray("to").add(body.getReceiver());
            requestBody.put("type", mapMessageType(body.getMessageType()));

            ObjectNode msgBody = requestBody.putObject("body");
            msgBody.put("msg", body.getContent());

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + appToken);
            headers.put("Content-Type", "application/json");

            String response = httpClient.postJson(url, headers, requestBody);
            log.info("环信 IM 发送成功: msgId={}", body.getMsgId());
            return response;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("环信 IM 发送异常", e);
        }
    }

    @Override
    public String generateToken(long userId) {
        // 动态 Token 算法（零网络开销，纯本地计算）
        String appKey = config.getAppKey();
        long curTime = System.currentTimeMillis() / 1000;
        int ttl = 600; // 10 分钟有效

        String str = config.getClientId() + appKey + userId + curTime + ttl + config.getClientSecret();
        String signature = SignatureUtils.sha256Hex(str);

        String json = String.format(
                "{\"signature\":\"%s\",\"appkey\":\"%s\",\"userId\":\"%s\",\"curTime\":%d,\"ttl\":%d}",
                signature, appKey, userId, curTime, ttl
        );

        return "dt-" + SignatureUtils.base64UrlEncode(json);
    }

    @Override
    public String refreshToken(long userId) {
        return generateToken(userId);
    }

    @Override
    public void ensureUserRegistered(long userId) {
        // 环信支持 autoCreateUser，无需单独注册
        // 首次发送消息或获取 Token 时可自动创建
        log.debug("环信 IM 无需单独注册用户: userId={}", userId);
    }

    @Override
    public String getChannel() {
        return Channel.EASEMOB.getCode();
    }

    // ===== 私有方法 =====

    /**
     * 获取 App Token（带缓存）
     */
    private String getAppToken() {
        if (cachedAppToken != null && System.currentTimeMillis() < appTokenExpireAt) {
            return cachedAppToken;
        }

        try {
            String url = String.format("%s/%s/%s/token", config.getHost(), config.getOrgName(), config.getAppName());

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("grant_type", "client_credentials");
            requestBody.put("client_id", config.getClientId());
            requestBody.put("client_secret", config.getClientSecret());

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            String response = httpClient.postJson(url, headers, requestBody);
            JsonNode json = httpClient.parseJson(response);

            cachedAppToken = json.path("access_token").asText();
            long expiresIn = json.path("expires_in").asLong(86400L * 5); // 默认 5 天
            appTokenExpireAt = System.currentTimeMillis() + (expiresIn * 1000) - 300_000; // 提前 5 分钟过期

            log.info("环信 IM App Token 获取成功，有效期: {}s", expiresIn);
            return cachedAppToken;
        } catch (Exception e) {
            throw new RuntimeException("环信 IM App Token 获取失败", e);
        }
    }

    private String mapMessageType(String messageType) {
        if ("text".equals(messageType)) return "txt";
        if ("image".equals(messageType)) return "img";
        if ("voice".equals(messageType)) return "audio";
        return "txt";
    }
}
