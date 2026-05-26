package com.clmcat.qianyu.user.support.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.clmcat.qianyu.user.support.LoginSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云国内短信发送器。
 */
@Slf4j
@Service
public class AliyunSmsSender {

    private final AliyunSmsProperties properties;

    private volatile Client client;

    public AliyunSmsSender(AliyunSmsProperties properties) {
        this.properties = properties;
    }

    /**
     * 发送国内短信。
     *
     * @param phoneNumbers 手机号，多个号码使用英文逗号分隔
     * @param signName 签名；为空时回落到配置里的默认签名
     * @param templateCode 模板编码
     * @param templateParam 模板参数 JSON 文本
     */
    public void send(String phoneNumbers, String signName, String templateCode, String templateParam) {
        validateEnabled();
        List<String> phoneList = normalizePhoneNumbers(phoneNumbers);
        validateDomesticPhones(phoneList);

        String resolvedSignName = StringUtils.hasText(signName) ? signName : properties.getSignName();
        if (!StringUtils.hasText(resolvedSignName)) {
            throw new IllegalArgumentException("短信签名不能为空");
        }
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalStateException("未配置阿里云短信模板编码");
        }

        SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(String.join(",", phoneList))
                .setSignName(resolvedSignName)
                .setTemplateCode(templateCode)
                .setTemplateParam(templateParam);

        SendSmsResponse response;
        try {
            response = getClient().sendSms(request);
        } catch (Exception e) {
            throw new IllegalStateException("阿里云短信发送失败", e);
        }

        if (response.getBody() == null) {
            throw new IllegalStateException("阿里云短信发送失败: 响应体为空");
        }

        String code = response.getBody().getCode();
        String message = response.getBody().getMessage();
        if (!"OK".equalsIgnoreCase(code)) {
            throw new IllegalStateException("阿里云短信发送失败: " + message);
        }

        String requestId = response.getBody().getRequestId();
        String bizId = response.getBody().getBizId();
        log.info("阿里云国内短信发送成功, phones={}, requestId={}, bizId={}", maskPhones(phoneList), requestId, bizId);
    }

    private void validateEnabled() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("阿里云短信发送未启用");
        }
        if (!StringUtils.hasText(properties.getAccessKeyId()) || !StringUtils.hasText(properties.getAccessKeySecret())) {
            throw new IllegalStateException("未配置阿里云短信 AccessKey");
        }
        if (!StringUtils.hasText(properties.getEndpoint())) {
            throw new IllegalStateException("未配置阿里云短信服务地址");
        }
    }

    private List<String> normalizePhoneNumbers(String phoneNumbers) {
        if (!StringUtils.hasText(phoneNumbers)) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        List<String> phoneList = new ArrayList<>();
        for (String part : phoneNumbers.split(",")) {
            String phone = part == null ? null : part.trim();
            if (!StringUtils.hasText(phone)) {
                continue;
            }
            if (phone.startsWith("+")) {
                String normalizedPhone = LoginSupport.normalizeCnSmsPhone(phone);
                if (normalizedPhone == null) {
                    throw new IllegalArgumentException("当前仅支持 +86 国内短信: " + phone);
                }
                phoneList.add(normalizedPhone);
                continue;
            }
            phoneList.add(phone);
        }
        if (phoneList.isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        return phoneList;
    }

    private void validateDomesticPhones(List<String> phoneList) {
        for (String phone : phoneList) {
            if (phone == null || !phone.matches("1\\d{10}")) {
                throw new IllegalArgumentException("当前仅支持 11 位中国大陆手机号: " + phone);
            }
        }
    }

    private Client getClient() {
        Client current = client;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (client == null) {
                client = createClient();
            }
            return client;
        }
    }

    private Client createClient() {
        Config config = new Config();
        config.setAccessKeyId(properties.getAccessKeyId());
        config.setAccessKeySecret(properties.getAccessKeySecret());
        config.setEndpoint(properties.getEndpoint());
        try {
            return new Client(config);
        } catch (Exception e) {
            throw new IllegalStateException("创建阿里云短信客户端失败", e);
        }
    }

    private String maskPhones(List<String> phoneList) {
        List<String> maskedPhones = new ArrayList<>();
        for (String phone : phoneList) {
            if (!StringUtils.hasText(phone) || phone.length() <= 7) {
                maskedPhones.add(phone);
                continue;
            }
            maskedPhones.add(phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4));
        }
        return String.join(",", maskedPhones);
    }
}
