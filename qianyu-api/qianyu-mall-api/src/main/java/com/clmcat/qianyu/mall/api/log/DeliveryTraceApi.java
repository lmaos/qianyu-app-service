package com.clmcat.qianyu.mall.api.log;

import com.clmcat.qianyu.mall.api.log.model.dto.LogDeliveryTraceDto;

import java.util.List;

/**
 * 物流轨迹 RPC 接口
 */
public interface DeliveryTraceApi {

    /**
     * 批量写入轨迹
     */
    void batchInsert(List<LogDeliveryTraceDto> traces);
}
