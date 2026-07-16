package com.clmcat.qianyu.mall.pms.rpc;

import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.api.pms.PmsSpuStatusLogApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuStatusLogDto;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuStatusLogMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpuStatusLog;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsSpuStatusLogTableDef.PMS_SPU_STATUS_LOG;

/**
 * SPU 状态变更流水查询 RPC 实现（只读）。
 */
@DubboService
@Service
public class PmsSpuStatusLogApiImpl implements PmsSpuStatusLogApi {

    @Resource
    private PmsSpuStatusLogMapper statusLogMapper;

    @Override
    public PageResultDTO<PmsSpuStatusLogDto> page(Long spuId, String event, String source,
                                                  Long startTime, Long endTime, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        QueryWrapper qw = QueryWrapper.create();
        if (spuId != null) {
            qw.and(PMS_SPU_STATUS_LOG.SPU_ID.eq(spuId));
        }
        if (event != null && !event.isBlank()) {
            qw.and(PMS_SPU_STATUS_LOG.EVENT.eq(event.trim()));
        }
        if (source != null && !source.isBlank()) {
            qw.and(PMS_SPU_STATUS_LOG.SOURCE.eq(source.trim()));
        }
        if (startTime != null) {
            qw.and(PMS_SPU_STATUS_LOG.CREATE_TIME.ge(startTime));
        }
        if (endTime != null) {
            qw.and(PMS_SPU_STATUS_LOG.CREATE_TIME.le(endTime));
        }
        qw.orderBy(PMS_SPU_STATUS_LOG.CREATE_TIME.desc(), PMS_SPU_STATUS_LOG.ID.desc());
        Page<PmsSpuStatusLog> page = statusLogMapper.paginate(Page.of(pageNum, pageSize), qw);
        List<PmsSpuStatusLogDto> records = page.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        return PageResultDTO.<PmsSpuStatusLogDto>builder()
                .records(records).total(page.getTotalRow())
                .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
    }

    private PmsSpuStatusLogDto toDto(PmsSpuStatusLog e) {
        PmsSpuStatusLogDto d = new PmsSpuStatusLogDto();
        d.setId(e.getId());
        d.setSpuId(e.getSpuId());
        d.setFromStatus(e.getFromStatus());
        d.setToStatus(e.getToStatus());
        d.setEvent(e.getEvent());
        d.setSource(e.getSource());
        d.setOperatorId(e.getOperatorId());
        d.setReason(e.getReason());
        d.setProcessed(e.getProcessed());
        d.setProcessTime(e.getProcessTime());
        d.setCreateTime(e.getCreateTime());
        return d;
    }
}
