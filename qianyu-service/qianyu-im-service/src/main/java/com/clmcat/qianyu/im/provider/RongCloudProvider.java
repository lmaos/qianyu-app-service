package com.clmcat.qianyu.im.provider;

import com.clmcat.qianyu.im.config.RongCloudImConfig;
import com.clmcat.qianyu.im.model.enums.Channel;
import com.clmcat.qianyu.im.model.enums.ChatType;
import com.clmcat.qianyu.im.model.body.MessageBody;
import com.clmcat.qianyu.im.util.ImHttpClient;
import com.clmcat.qianyu.im.util.SignatureUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 融云 IM Provider
 *
 * REST API 鉴权: SHA1 签名，4 个 Header
 * Signature = SHA1(AppSecret + Nonce + Timestamp)
 */
@Slf4j
@Service
public class RongCloudProvider implements IMProvider {

    @Resource
    private RongCloudImConfig config;

    @Resource
    private ImHttpClient httpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String sendMessage(MessageBody body) {
        try {
            String url;
            if (body.getChatType() != null && body.getChatType() == ChatType.GROUP.getCode()) {
                url = config.getApiUrl() + "/message/group/publish.json";
            } else {
                url = config.getApiUrl() + "/message/private/publish.json";
            }

            Map<String, String> headers = buildSignatureHeaders();

            Map<String, String> formData = new HashMap<>();
            formData.put("fromUserId", body.getSender());
            formData.put("toUserId", body.getReceiver());
            formData.put("objectName", "RC:TxtMsg");
            // 融云消息内容为 JSON 字符串
            String contentJson = String.format("{\"content\":\"%s\"}", escapeJson(body.getContent()));
            formData.put("content", contentJson);

            String response = httpClient.postForm(url, headers, formData);
            JsonNode json = httpClient.parseJson(response);

            int code = json.path("code").asInt(-1);
            if (code != 200) {
                throw new RuntimeException("融云 IM 发送失败: code=" + code);
            }

            log.info("融云 IM 发送成功: msgId={}", body.getMsgId());
            return response;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("融云 IM 发送异常", e);
        }
    }

    @Override
    public String generateToken(long userId) {
        try {
            // 融云 getToken 等同于注册+获取 Token
            ensureUserRegistered(userId);

            String url = config.getApiUrl() + "/user/getToken.json";
            Map<String, String> headers = buildSignatureHeaders();

            Map<String, String> formData = new HashMap<>();
            formData.put("userId", String.valueOf(userId));
            formData.put("name", String.valueOf(userId));

            String response = httpClient.postForm(url, headers, formData);
            JsonNode json = httpClient.parseJson(response);

            int code = json.path("code").asInt(-1);
            if (code != 200) {
                throw new RuntimeException("融云 IM 获取 Token 失败: code=" + code);
            }

            return json.path("token").asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("融云 IM 获取 Token 异常", e);
        }
    }

    @Override
    public String refreshToken(long userId) {
        return generateToken(userId);
    }

    @Override
    public void ensureUserRegistered(long userId) {
        // 融云 getToken 接口同时注册用户，无需单独注册
        log.debug("融云 IM 注册通过 getToken 自动完成: userId={}", userId);
    }

    @Override
    public String getChannel() {
        return Channel.RONGCLOUD.getCode();
    }

    // ===== 私有方法 =====

    /**
     * 构建融云 API 签名 Header
     * App-Key + Nonce + Timestamp + Signature
     */
    private Map<String, String> buildSignatureHeaders() {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());

        String signature = SignatureUtils.sha1Hex(config.getAppSecret() + nonce + timestamp);

        Map<String, String> headers = new HashMap<>();
        headers.put("App-Key", config.getAppKey());
        headers.put("Nonce", nonce);
        headers.put("Timestamp", timestamp);
        headers.put("Signature", signature);
        return headers;
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
