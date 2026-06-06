package com.clmcat.qianyu.im.provider;

import com.clmcat.qianyu.im.config.TencentImConfig;
import com.clmcat.qianyu.im.model.enums.Channel;
import com.clmcat.qianyu.im.model.body.MessageBody;
import com.clmcat.qianyu.im.util.ImHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tencentyun.TLSSigAPIv2;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 腾讯云 IM Provider
 *
 * REST API 鉴权: URL 参数中携带 sdkappid + identifier + usersig
 * UserSig 生成: HMAC-SHA256 (TLSSigAPIv2 SDK)
 */
@Slf4j
@Service
public class TencentProvider implements IMProvider {

    @Resource
    private TencentImConfig config;

    @Resource
    private ImHttpClient httpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    /**
     * 进程内已注册用户集合（用于 ensureUserRegistered 幂等）。
     * 注册成功（含腾讯云返回 70001 账号已存在）后加入，下次直接跳过 REST 调用，
     * 避免同一用户重复触发腾讯云 account_import 限频。
     */
    private final Set<String> registeredUserIds = ConcurrentHashMap.newKeySet();

    @Override
    public String sendMessage(MessageBody body) {
        try {
            String adminUserSig = generateAdminUserSig();
            String url = buildSendUrl(adminUserSig);

            ObjectNode requestBody = buildSendRequestBody(body);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            String response = httpClient.postJson(url, headers, requestBody);
            JsonNode json = httpClient.parseJson(response);

            // 校验响应
            String actionStatus = json.path("ActionStatus").asText();
            int errorCode = json.path("ErrorCode").asInt(-1);

            if (!"OK".equals(actionStatus) || errorCode != 0) {
                String errorInfo = json.path("ErrorInfo").asText("未知错误");
                throw new RuntimeException("腾讯云 IM 发送失败: ErrorCode=" + errorCode + " " + errorInfo);
            }

            log.info("腾讯云 IM 发送成功: msgId={}", body.getMsgId());
            return response;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("腾讯云 IM 发送异常", e);
        }
    }

    @Override
    public String generateToken(long userId) {
        TLSSigAPIv2 gen = new TLSSigAPIv2(config.getSdkAppId(), config.getSecretKey());
        return gen.genUserSig(String.valueOf(userId), config.getUserSigExpireSeconds());
    }

    @Override
    public String refreshToken(long userId) {
        // UserSig 是无状态签名，每次重新生成即可
        return generateToken(userId);
    }

    @Override
    public void ensureUserRegistered(long userId) {
        String identifier = String.valueOf(userId);

        // 幂等：已注册过的用户直接跳过，避免反复打腾讯云 account_import 触发 REST 限频
        if (registeredUserIds.contains(identifier)) {
            return;
        }

        try {
            String adminUserSig = generateAdminUserSig();
            String url = buildApiUrl("v4/im_open_login_svc/account_import", adminUserSig);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("Identifier", identifier);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            String response = httpClient.postJson(url, headers, body);
            JsonNode json = httpClient.parseJson(response);

            String actionStatus = json.path("ActionStatus").asText();
            int errorCode = json.path("ErrorCode").asInt(-1);
            String errorInfo = json.path("ErrorInfo").asText("");

            if ("OK".equals(actionStatus)) {
                // 注册成功
                registeredUserIds.add(identifier);
                log.info("腾讯云 IM 用户注册成功: userId={}", userId);
            } else if (errorCode == 70001) {
                // 70001 = 账号已存在，腾讯云幂等返回，视为成功
                registeredUserIds.add(identifier);
                log.info("腾讯云 IM 用户已存在: userId={}", userId);
            } else {
                // 其他错误：记 warn 但不抛异常（用户没注册不会阻塞本次业务，后续 SDK 登录时再注册）
                log.warn("腾讯云 IM 用户注册失败: userId={}, errorCode={}, errorInfo={}", userId, errorCode, errorInfo);
            }
        } catch (Exception e) {
            log.warn("腾讯云 IM 用户注册异常（不影响业务）: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public String getChannel() {
        return Channel.TENCENT.getCode();
    }

    @Override
    public long getSdkAppId() {
        return config.getSdkAppId();
    }

    // ===== 私有方法 =====

    private String generateAdminUserSig() {
        TLSSigAPIv2 gen = new TLSSigAPIv2(config.getSdkAppId(), config.getSecretKey());
        return gen.genUserSig(config.getAdminIdentifier(), 86400L * 30); // 管理员签名 30 天有效
    }

    private String buildSendUrl(String adminUserSig) {
        int randomInt = Math.abs(random.nextInt());
        return String.format(
                "https://console.tim.qq.com/v4/openim/sendmsg?sdkappid=%d&identifier=%s&usersig=%s&random=%d&contenttype=json",
                config.getSdkAppId(), config.getAdminIdentifier(), adminUserSig, randomInt
        );
    }

    private String buildApiUrl(String service, String adminUserSig) {
        int randomInt = Math.abs(random.nextInt());
        return String.format(
                "https://console.tim.qq.com/%s?sdkappid=%d&identifier=%s&usersig=%s&random=%d&contenttype=json",
                service, config.getSdkAppId(), config.getAdminIdentifier(), adminUserSig, randomInt
        );
    }

    private ObjectNode buildSendRequestBody(MessageBody body) {
        ObjectNode root = objectMapper.createObjectNode();

        root.put("From_Account", body.getSender());
        root.put("To_Account", body.getReceiver());
        root.put("MsgRandom", Math.abs(random.nextInt()));
        // 腾讯云要求秒级时间戳
        root.put("MsgTimeStamp", body.getClientTime() != null ? body.getClientTime() / 1000 : System.currentTimeMillis() / 1000);

        // 消息体
        ArrayNode msgBody = root.putArray("MsgBody");
        ObjectNode textElem = msgBody.addObject();
        textElem.put("MsgType", "TIMTextElem");
        ObjectNode msgContent = textElem.putObject("MsgContent");
        msgContent.put("Text", body.getContent());

        return root;
    }
}
