package com.clmcat.qianyu.mall.pay.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = "qianyu.mall.pay")
@Data
public class PayConfig implements InitializingBean {

    private Sandbox sandbox = new Sandbox();
    private Alipay alipay = new Alipay();
    private Wxpay wxpay = new Wxpay();
    private Timeout timeout = new Timeout();

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("sandbox:{}",sandbox);
        log.info("alipay:{}",alipay);
        log.info("wxpay:{}",wxpay);
    }

    @Data
    public static class Sandbox {
        private boolean open = true;
        private String mode = "success";
    }

    @Data
    public static class Alipay {
        private String appId = "demo-alipay";
        /** S13: 支付宝应用私钥（PKCS8 格式，用于回调验签） */
        private String privateKey = "";
        /** S13: 支付宝公钥（用于回调验签 rsaCheckV1） */
        private String publicKey = "";
        /** S13: 签名算法类型 RSA2 / RSA */
        private String signType = "RSA2";
        /** S13: 异步通知地址（notify_url） */
        private String notifyUrl = "";
    }

    @Data
    public static class Wxpay {
        private String appId = "demo-wxpay1";
        /** S13: 微信支付商户号 */
        private String merchantId = "";
        /** S13: 商户 API 证书序列号 */
        private String merchantSerial = "";
        /** S13: 商户 API V3 私钥（PEM 格式 apiclient_key.pem 内容） */
        private String privateKey = "";
        /** S13: API V3 密钥（用于回调解密 AES-256-GCM） */
        private String apiV3Key = "";
        /** S13: 异步通知地址（notify_url） */
        private String notifyUrl = "";
    }

    @Data
    public static class Timeout {
        private int minutes = 30;
    }
}
