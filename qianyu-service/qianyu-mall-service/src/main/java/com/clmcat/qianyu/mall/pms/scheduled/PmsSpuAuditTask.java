package com.clmcat.qianyu.mall.pms.scheduled;

import com.clmcat.qianyu.mall.pms.config.PmsConfig;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.service.PmsSpuStatusChanger;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品审核定时任务（stub）。
 *
 * <p>把「待审核({@link PmsSpu#STATUS_PENDING_AUDIT}=4)」推进为「审核通过({@link PmsSpu#STATUS_APPROVED}=5)」，
 * 模拟审核闸。链路：商户「提交审核」→ status=4 → <b>本任务</b> → status=5 → 商户「上架」→ status=1（C 端可见，
 * 自动进分类/搜索/推荐位）。
 *
 * <p><b>当前为 stub</b>：无条件自动通过 + 可选延迟（{@code delay-seconds} 模拟审核耗时）。
 * 未来替换为「自动审核（规则引擎/关键词/图片审核 API）+ 人工审核（运营后台审核队列）」——届时只换任务体，状态机不变。
 *
 * <p>并发：PmsSpu 无 version 字段，用 {@code WHERE id=? AND status=4} 作 CAS（status 自带乐观语义），
 * 跨 tick/重复执行不会双推进（第二次 status≠4，affected=0）。仿
 * {@link com.clmcat.qianyu.mall.oms.scheduled.OmsOrderTimeoutTask} 的「查询待处理 + 逐条 CAS + 单条异常捕获」模式。
 */
@Slf4j
@Component
public class PmsSpuAuditTask {

    @Resource
    private PmsSpuMapper spuMapper;

    @Resource
    private PmsConfig pmsConfig;

    @Resource
    private PmsSpuStatusChanger spuStatusChanger;

    /**
     * 扫描待审核商品并推进为审核通过。间隔由 {@code qianyu.mall.pms.audit.rate-ms} 配置（默认 30s）。
     */
    @Scheduled(fixedRateString = "${qianyu.mall.pms.audit.rate-ms:30000}")
    public void autoApprove() {
        long delayMs = pmsConfig.getAudit().getDelaySeconds() * 1000L;
        long threshold = System.currentTimeMillis() - delayMs;
        // 待审核(4) 且已过延迟闸（提交后等待 delay-seconds；delay=0 则 threshold=now，立即通过）
        List<PmsSpu> pending = spuMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = ?", PmsSpu.STATUS_PENDING_AUDIT)
                        .and("update_time <= ?", threshold)
                        .and("deleted = 0")
        );
        if (pending.isEmpty()) return;

        log.info("商品审核(stub): 发现 {} 个待审核商品（延迟 {} 秒）", pending.size(), pmsConfig.getAudit().getDelaySeconds());
        int approved = 0;
        for (PmsSpu spu : pending) {
            try {
                // CAS 推进 4 → 5（changer 内 WHERE id + status=4；affected=0 表示已被其它流程改动，跳过）
                // 状态写入 + 流水收敛在 changer.casApprovePending（source=SYSTEM）
                if (spuStatusChanger.casApprovePending(spu.getId())) {
                    approved++;
                } else {
                    log.info("商品审核跳过(状态已变更): spuId={}", spu.getId());
                }
            } catch (Exception e) {
                log.error("商品审核失败: spuId={}, error={}", spu.getId(), e.getMessage());
            }
        }
        log.info("商品审核(stub): 本次推进 {} 个为审核通过", approved);
    }
}
