package com.clmcat.qianyu.user.support.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云国内短信配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "qianyu.sms.aliyun")
public class AliyunSmsProperties {

    /**
     * 是否启用阿里云短信发送。
     */
    private boolean enabled;

    /**
     * AccessKey ID。
     */
    private String accessKeyId;

    /**
     * AccessKey Secret。
     */
    private String accessKeySecret;

    /**
     * 国内短信服务地址。
     */
    private String endpoint = "dysmsapi.aliyuncs.com";

    /**
     * 默认短信签名。
     */
    private String signName;
}
