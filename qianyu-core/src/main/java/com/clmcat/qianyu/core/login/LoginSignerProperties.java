package com.clmcat.qianyu.core.login;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@ConfigurationProperties(prefix = "qianyu.login.signer")
@Component
@Data
public class LoginSignerProperties {
    /**
     * 私钥配置路径, 如果制定了私钥路径，则优先从该路径读取。 (高优先级)
     */
    private String privateKeyPath;
    /**
     * Base64 私钥配置
     */
    private String privateKey;

    /**
     * Token 过期时间，单位毫秒，默认 7 天
     */
    private long expireMillis =  TimeUnit.DAYS.toMillis(7);

}
