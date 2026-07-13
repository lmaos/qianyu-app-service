package com.clmcat.qianyu.mall.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * M5: 物流配置。tracker=nil 时用 Mock假数据。
 *
 * <pre>
 * qianyu:
 *   mall:
 *     logistics:
 *       tracker: nil        # nil=Mock, kdniao=快递鸟, kuaidi100=快递100
 *       kdniao:
 *         business-id: ""
 *         api-key: ""
 *       kuaidi100:
 *         customer: ""
 *         key: ""
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "qianyu.mall.logistics")
@Data
public class LogisticsConfig {

    /** 物流轨迹提供商：nil=Mock, kdniao=快递鸟, kuaidi100=快递100 */
    private String tracker = "nil";
    private Kdniao kdniao = new Kdniao();
    private Kuaidi100 kuaidi100 = new Kuaidi100();

    @Data
    public static class Kdniao {
        private String businessId = "";
        private String apiKey = "";
    }

    @Data
    public static class Kuaidi100 {
        private String customer = "";
        private String key = "";
    }
}
