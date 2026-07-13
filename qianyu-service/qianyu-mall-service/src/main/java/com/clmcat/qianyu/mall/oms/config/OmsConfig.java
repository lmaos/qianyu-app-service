package com.clmcat.qianyu.mall.oms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单域配置。仿 {@link com.clmcat.qianyu.mall.pay.config.PayConfig} 模式。
 *
 * <pre>
 * qianyu:
 *   mall:
 *     oms:
 *       auto-receive:
 *         days: 15   # 发货后自动确认收货天数
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "qianyu.mall.oms")
@Data
public class OmsConfig {

    private AutoReceive autoReceive = new AutoReceive();

    @Data
    public static class AutoReceive {
        /** 发货后自动确认收货天数（超时未确认则 CAS 30→40） */
        private int days = 15;
    }
}
