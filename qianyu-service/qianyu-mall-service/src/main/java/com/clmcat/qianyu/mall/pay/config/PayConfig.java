package com.clmcat.qianyu.mall.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付配置（sandbox 沙箱模式）。
 *
 * qianyu.mall.pay.sandbox.open  = true  → 不拉起第三方支付，直接按 mode 处理
 * qianyu.mall.pay.sandbox.mode  = success → 默认成功；fail → 默认失败；realmode → 走真实回调
 */
@Component
@ConfigurationProperties(prefix = "qianyu.mall.pay")
@Data
public class PayConfig {

    private Sandbox sandbox = new Sandbox();
    private Alipay alipay = new Alipay();
    private Wxpay wxpay = new Wxpay();
    private Timeout timeout = new Timeout();

    @Data
    public static class Sandbox {
        /** true=沙箱（不拉起支付）；false=真实支付 */
        private boolean open = true;
        /** success=默认成功；fail=默认失败；realmode=真实回调 */
        private String mode = "success";
    }

    @Data
    public static class Alipay {
        private String appId = "demo-alipay";
    }

    @Data
    public static class Wxpay {
        private String appId = "demo-wxpay";
    }

    @Data
    public static class Timeout {
        /** 订单支付超时时间（分钟），超时后自动取消并释放库存 */
        private int minutes = 30;
    }
}
