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

    /**
     * 物流回调验签密钥（trackPush 须在 sign 字段携带本值）。
     * <p>当前为通用 token 占位：{@code MessageDigest.isEqual(sign, key)} 常量时间比较。
     * 留空则 fail-closed（拒绝所有 push，避免误配置反而敞开口子）。
     * 将来接快递100/快递鸟官方订阅时，替换为对应官方签名算法。
     */
    private String callbackSignKey = "";

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
