package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuStatusLogMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpuStatusLog;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

/**
 * SPU 状态变更的唯一入口：状态写入 + {@link PmsSpuStatusLog} 流水写入收敛在此，
 * 供楼层自动投放任务消费。所有改 SPU status 的代码都必须走本类，禁止裸 {@code spuMapper.update/.updateByQuery} 改 status。
 *
 * <p>event 字典：LIST_ON / LIST_OFF / SUBMIT_AUDIT / AUDIT_PASS / AUDIT_REGRESS / EDIT_REGRESS / CREATE。
 * source 字典：MERCHANT / ADMIN / SYSTEM。
 */
@Slf4j
@Service
public class PmsSpuStatusChanger {

    @Resource
    private PmsSpuMapper spuMapper;
    @Resource
    private PmsSpuStatusLogMapper statusLogMapper;
    @Resource
    private PmsSupport pmsSupport;

    /**
     * 常规状态变更：加载旧状态 → 单次 update（status + extraMutator 附带字段）→ 写流水。
     * 与调用方同事务（调用方多为 @Transactional），流水失败则整体回滚（保证不丢事件）。
     *
     * @param extraMutator 在 changer 构造的 update 对象上追加字段（如 setPublishTime）；可为 null
     * @return 旧状态；SPU 不存在返回 null（调用方按需校验）
     */
    @Transactional
    public Integer change(Long spuId, int toStatus, String event, String source,
                          Long operatorId, String reason, Consumer<PmsSpu> extraMutator) {
        PmsSpu old = spuMapper.selectOneById(spuId);
        if (old == null) {
            return null;
        }
        Integer fromStatus = old.getStatus();
        long now = System.currentTimeMillis();
        PmsSpu update = new PmsSpu();
        update.setId(spuId);
        update.setStatus(toStatus);
        update.setUpdateTime(now);
        if (extraMutator != null) {
            extraMutator.accept(update);
        }
        spuMapper.update(update);
        writeLog(spuId, fromStatus, toStatus, event, source, operatorId, reason, now);
        return fromStatus;
    }

    /**
     * CAS 推进「待审核(4) → 审核通过(5)」（仿 PmsSpuAuditTask 的 WHERE status=4 乐观语义）。
     * affected>0（即本次由本调用完成推进）才写流水，避免重复 tick 双写。
     *
     * @return true=本次推进成功
     */
    @Transactional
    public boolean casApprovePending(Long spuId) {
        long now = System.currentTimeMillis();
        PmsSpu update = new PmsSpu();
        update.setStatus(PmsSpu.STATUS_APPROVED);
        update.setUpdateTime(now);
        int affected = spuMapper.updateByQuery(update,
                QueryWrapper.create().where("id = ?", spuId).and("status = ?", PmsSpu.STATUS_PENDING_AUDIT));
        if (affected > 0) {
            writeLog(spuId, PmsSpu.STATUS_PENDING_AUDIT, PmsSpu.STATUS_APPROVED,
                    "AUDIT_PASS", "SYSTEM", null, null, now);
            return true;
        }
        return false;
    }

    /** 新建草稿记一条 CREATE（审计用；不影响投放，to_status=0 不被任务消费）。 */
    public void recordCreate(Long spuId, String source, Long operatorId) {
        writeLog(spuId, null, PmsSpu.STATUS_DRAFT, "CREATE", source, operatorId, null, System.currentTimeMillis());
    }

    private void writeLog(Long spuId, Integer fromStatus, int toStatus, String event, String source,
                          Long operatorId, String reason, long now) {
        PmsSpuStatusLog row = new PmsSpuStatusLog();
        row.setId(pmsSupport.nextId());
        row.setSpuId(spuId);
        row.setFromStatus(fromStatus);
        row.setToStatus(toStatus);
        row.setEvent(event);
        row.setSource(source);
        row.setOperatorId(operatorId);
        row.setReason(reason);
        row.setProcessed(PmsSpuStatusLog.PROCESSED_NO);
        row.setCreateTime(now);
        statusLogMapper.insertSelective(row);
    }
}
