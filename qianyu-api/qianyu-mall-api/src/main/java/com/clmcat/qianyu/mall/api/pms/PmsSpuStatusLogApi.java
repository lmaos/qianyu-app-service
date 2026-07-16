package com.clmcat.qianyu.mall.api.pms;

import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuStatusLogDto;

/**
 * SPU 状态变更流水查询 RPC 接口 — 运营后台审计/排查用（只读）。
 */
public interface PmsSpuStatusLogApi {

    /**
     * 分页查询状态流水。
     *
     * @param spuId    精确过滤（可空）
     * @param event    事件过滤（可空）：LIST_ON/LIST_OFF/SUBMIT_AUDIT/AUDIT_PASS/...
     * @param source   来源过滤（可空）：MERCHANT/ADMIN/SYSTEM
     * @param startTime / endTime 创建时间区间（毫秒，可空）
     */
    PageResultDTO<PmsSpuStatusLogDto> page(Long spuId, String event, String source,
                                           Long startTime, Long endTime, int pageNum, int pageSize);
}
