package com.clmcat.qianyu.mall.cms.scheduled;

import com.clmcat.qianyu.mall.cms.mapper.CmsZoneProductMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsZoneProduct;
import com.clmcat.qianyu.mall.cms.service.ZonePlacementDecider;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuStatusLogMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpuStatusLog;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsZoneProductTableDef.CMS_ZONE_PRODUCT;
import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsSpuStatusLogTableDef.PMS_SPU_STATUS_LOG;

/**
 * 楼层自动投放任务。消费 {@link PmsSpuStatusLog}（processed=0）：
 * <ul>
 *   <li>to_status=1（上架）：调 {@link ZonePlacementDecider} 决策 → 写 {@code cms_zone_product}(source=AUTO)。</li>
 *   <li>to_status=2（下架）：逻辑删除该 SPU 的所有 AUTO 行（手动行保留）。</li>
 *   <li>其余事件（提审/审核/创建等）：不处理，仅标记 processed=1。</li>
 * </ul>
 * 仿 {@code PmsSpuAuditTask} 的「查待处理 + 逐条处理 + 单条异常捕获」模式。间隔由
 * {@code qianyu.mall.cms.zone-fill.rate-ms} 配置（默认 60s）。
 */
@Slf4j
@Component
public class CmsZoneAutoFillTask {

    private static final int BATCH_LIMIT = 100;

    @Resource
    private PmsSpuStatusLogMapper statusLogMapper;
    @Resource
    private PmsSpuMapper spuMapper;
    @Resource
    private CmsZoneProductMapper zoneProductMapper;
    @Resource
    private ZonePlacementDecider placementDecider;
    @Resource
    private PmsSupport pmsSupport;

    @Scheduled(fixedRateString = "${qianyu.mall.cms.zone-fill.rate-ms:60000}")
    public void autoFill() {
        List<PmsSpuStatusLog> pending = statusLogMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(PMS_SPU_STATUS_LOG.PROCESSED.eq(PmsSpuStatusLog.PROCESSED_NO))
                        .orderBy(PMS_SPU_STATUS_LOG.CREATE_TIME.asc())
                        .limit(BATCH_LIMIT));
        if (pending == null || pending.isEmpty()) {
            return;
        }
        int done = 0;
        for (PmsSpuStatusLog row : pending) {
            try {
                processOne(row);
                done++;
            } catch (Exception e) {
                log.error("楼层投放处理失败: logId={}, spuId={}, error={}", row.getId(), row.getSpuId(), e.getMessage());
            }
        }
        if (done > 0) {
            log.info("楼层投放(stub): 本次消费 {} 条状态流水", done);
        }
    }

    private void processOne(PmsSpuStatusLog row) {
        long now = System.currentTimeMillis();
        Integer to = row.getToStatus();
        if (to != null && to == PmsSpu.STATUS_ON_SALE) {
            PmsSpu spu = spuMapper.selectOneById(row.getSpuId());
            if (spu != null && spu.getStatus() != null && spu.getStatus() == PmsSpu.STATUS_ON_SALE) {
                List<ZonePlacementDecider.Placement> places = placementDecider.decide(spu);
                if (places != null) {
                    for (ZonePlacementDecider.Placement p : places) {
                        placeAuto(spu.getId(), p, now);
                    }
                }
            }
        } else if (to != null && to == PmsSpu.STATUS_OFF_SHELF) {
            // 下架：逻辑删除该 SPU 的所有 AUTO 行（手动行保留；下架期间本就不展示）
            zoneProductMapper.deleteByQuery(QueryWrapper.create()
                    .where(CMS_ZONE_PRODUCT.SPU_ID.eq(row.getSpuId()))
                    .and(CMS_ZONE_PRODUCT.SOURCE.eq(CmsZoneProduct.SOURCE_AUTO)));
        }
        markProcessed(row.getId(), now);
    }

    /** 写一条 AUTO 行；UK(zone_id, spu_id) 冲突则跳过（已存在）。 */
    private void placeAuto(Long spuId, ZonePlacementDecider.Placement p, long now) {
        CmsZoneProduct item = new CmsZoneProduct();
        long id = pmsSupport.nextId();
        item.setId(id);
        item.setZoneId(p.zoneId());
        item.setSpuId(spuId);
        item.setSort(p.sort() != null ? p.sort() : 0);
        item.setStatus(CmsZoneProduct.STATUS_SHOW);
        item.setSource(CmsZoneProduct.SOURCE_AUTO);
        item.setCreateTime(pmsSupport.parseTime(id));
        item.setUpdateTime(now);
        item.setDeleted(0);
        try {
            zoneProductMapper.insertSelective(item);
        } catch (DuplicateKeyException ignore) {
            // 该 (zone, spu) 已存在关联，跳过
        }
    }

    private void markProcessed(Long logId, long now) {
        PmsSpuStatusLog update = new PmsSpuStatusLog();
        update.setId(logId);
        update.setProcessed(PmsSpuStatusLog.PROCESSED_YES);
        update.setProcessTime(now);
        statusLogMapper.update(update);
    }
}
