package com.clmcat.qianyu.im.provider;

import com.clmcat.qianyu.im.config.NimImConfig;
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
 * 网易云信 IM Provider
 *
 * REST API 鉴权: SHA1 CheckSum，4 个 Header
 * CheckSum = SHA1(AppSecret + Nonce + CurTime)
 * 注意：CheckSum 有效期仅 5 分钟
 */
@Slf4j
@Service
public class NimProvider implements IMProvider {

    @Resource
    private NimImConfig config;

    @Resource
    private ImHttpClient httpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String sendMessage(MessageBody body) {
        try {
            String url = config.getApiUrl() + "/nimserver/msg/sendMsg.action";
            Map<String, String> headers = buildChecksumHeaders();

            int ope = (body.getChatType() != null && body.getChatType() == ChatType.GROUP.getCode()) ? 1 : 0;

            // 网易云信消息体为 JSON 字符串
            String msgBodyJson = String.format("{\"msg\":\"%s\"}", escapeJson(body.getContent()));

            Map<String, String> formData = new HashMap<>();
            formData.put("from", body.getSender());
            formData.put("ope", String.valueOf(ope));
            formData.put("to", body.getReceiver());
            formData.put("type", "0"); // 0=文本
            formData.put("body", msgBodyJson);

            String response = httpClient.postForm(url, headers, formData);
            JsonNode json = httpClient.parseJson(response);

            int code = json.path("code").asInt(-1);
            if (code != 200) {
                throw new RuntimeException("网易云信 IM 发送失败: code=" + code);
            }

            log.info("网易云信 IM 发送成功: msgId={}", body.getMsgId());
            return response;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("网易云信 IM 发送异常", e);
        }
    }

    @Override
    public String generateToken(long userId) {
        try {
            String url = config.getApiUrl() + "/nimserver/user/create.action";
            Map<String, String> headers = buildChecksumHeaders();

            Map<String, String> formData = new HashMap<>();
            formData.put("accid", String.valueOf(userId));
            formData.put("name", String.valueOf(userId));

            String response = httpClient.postForm(url, headers, formData);
            JsonNode json = httpClient.parseJson(response);

            int code = json.path("code").asInt(-1);
            if (code != 200) {
                throw new RuntimeException("网易云信 IM 创建用户失败: code=" + code);
            }

            // 414 = 用户已存在
            String token = json.path("info").path("token").asText();
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("网易云信 IM 创建用户返回 Token 为空");
            }

            return token;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("网易云信 IM 创建用户异常", e);
        }
    }

    @Override
    public String refreshToken(long userId) {
        try {
            String url = config.getApiUrl() + "/nimserver/user/refreshToken.action";
            Map<String, String> headers = buildChecksumHeaders();

            Map<String, String> formData = new HashMap<>();
            formData.put("accid", String.valueOf(userId));

            String response = httpClient.postForm(url, headers, formData);
            JsonNode json = httpClient.parseJson(response);

            int code = json.path("code").asInt(-1);
            if (code != 200) {
                throw new RuntimeException("网易云信 IM 刷新 Token 失败: code=" + code);
            }

            return json.path("info").path("token").asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("网易云信 IM 刷新 Token 异常", e);
        }
    }

    @Override
    public void ensureUserRegistered(long userId) {
        // create.action 会自动处理已存在用户
        // 先尝试 generateToken（内部调用 create.action）
        try {
            generateToken(userId);
        } catch (Exception e) {
            // 用户可能已存在，忽略错误
            log.debug("网易云信 IM 用户可能已存在: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public String getChannel() {
        return Channel.NIM.getCode();
    }

    // ===== 私有方法 =====

    /**
     * 构建网易云信 API 签名 Header
     * AppKey + Nonce + CurTime + CheckSum
     */
    private Map<String, String> buildChecksumHeaders() {
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        String curTime = String.valueOf(System.currentTimeMillis() / 1000);

        String checkSum = SignatureUtils.sha1Hex(config.getAppSecret() + nonce + curTime);

        Map<String, String> headers = new HashMap<>();
        headers.put("AppKey", config.getAppKey());
        headers.put("Nonce", nonce);
        headers.put("CurTime", curTime);
        headers.put("CheckSum", checkSum);
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        return headers;
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
