package com.clmcat.qianyu.mall.pay.scheduled;

import com.clmcat.qianyu.mall.pay.config.PayConfig;
import com.clmcat.qianyu.mall.pay.mapper.PayPaymentMapper;
import com.clmcat.qianyu.mall.pay.model.entity.PayPayment;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * S6(#8): 支付单超时回收任务（stub 级，自动关闭 PENDING）。
 * <p>扫描 PENDING 状态超时未完成的支付单，CAS 关闭（PENDING→CLOSED），使订单可被重新支付或由
 * {@link com.clmcat.qianyu.mall.oms.scheduled.OmsOrderTimeoutTask} 取消释放库存。
 * <p>超时阈值复用 {@code qianyu.mall.pay.timeout.minutes}（与订单支付超时同口径）。仿 OmsOrderTimeoutTask 模式。
 */
@Slf4j
@Component
public class PayPaymentTimeoutTask {

    @Resource
    private PayPaymentMapper paymentMapper;

    @Resource
    private PayConfig payConfig;

    /**
     * 每 60 秒扫描一次，关闭超时 PENDING 支付单。
     */
    @Scheduled(fixedRate = 60_000)
    public void closeTimeoutPendingPayments() {
        long timeoutMs = payConfig.getTimeout().getMinutes() * 60_000L;
        long threshold = System.currentTimeMillis() - timeoutMs;
        List<PayPayment> pending = paymentMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("pay_status = ?", PayPayment.PAY_STATUS_PENDING)
                        .and("create_time < ?", threshold)
                        .and("deleted = 0"));
        if (pending == null || pending.isEmpty()) return;

        log.info("支付单超时回收: 发现 {} 笔超时 PENDING 支付单（超时 {} 分钟）", pending.size(), payConfig.getTimeout().getMinutes());
        long now = System.currentTimeMillis();
        int closed = 0;
        for (PayPayment p : pending) {
            try {
                // CAS 关闭：WHERE id + pay_status=PENDING，防并发双关
                PayPayment update = new PayPayment();
                update.setPayStatus(PayPayment.PAY_STATUS_CLOSED);
                update.setUpdateTime(now);
                int rows = paymentMapper.updateByQuery(update,
                        QueryWrapper.create().where("id = ?", p.getId())
                                .and("pay_status = ?", PayPayment.PAY_STATUS_PENDING));
                if (rows > 0) closed++;
            } catch (Exception e) {
                log.error("支付单超时关闭失败 paymentId={} error={}", p.getId(), e.getMessage());
            }
        }
        log.info("支付单超时回收: 本次关闭 {} 笔", closed);
    }
}
