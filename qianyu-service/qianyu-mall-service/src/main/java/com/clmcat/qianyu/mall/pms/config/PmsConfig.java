package com.clmcat.qianyu.mall.pms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 商品域配置。
 *
 * <p>审核任务（stub，自动通过）：当前无条件把「待审核(4)」推进为「审核通过(5)」，
 * 未来替换为「自动审核（规则/关键词/图片）+ 人工审核（运营后台）」。仿
 * {@link com.clmcat.qianyu.mall.pay.config.PayConfig} 的 @ConfigurationProperties 模式。
 *
 * <pre>
 * qianyu:
 *   mall:
 *     pms:
 *       audit:
 *         rate-ms: 30000        # 审核任务扫描间隔(毫秒)
 *         delay-seconds: 10     # 模拟审核耗时(秒)，提交后 N 秒才自动通过；0=立即
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "qianyu.mall.pms")
@Data
public class PmsConfig {

    private Audit audit = new Audit();

    @Data
    public static class Audit {
        /** 审核任务扫描间隔（毫秒） */
        private long rateMs = 30_000;
        /** 模拟审核耗时（秒）：提交审核后 N 秒才自动通过；0=立即通过 */
        private long delaySeconds = 10;
    }
}
