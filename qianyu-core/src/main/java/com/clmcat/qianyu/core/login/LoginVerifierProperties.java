package com.clmcat.qianyu.core.login;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "qianyu.login.verifier")
public class LoginVerifierProperties {
    /**
     * 公钥储存的路径， 如果指定了公钥路径，则会从该路径加载公钥进行验证；如果没有指定，则会尝试从 base64 公钥字符串加载。
     * <br>
     * 高优先级
     */
    private String publicKeyPath;
    /**
     * Base64公钥串
     */
    private String publicKey;


}
