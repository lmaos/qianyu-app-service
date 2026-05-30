package com.clmcat.qianyu.mall.api.log;

import com.clmcat.qianyu.mall.api.log.model.dto.LogShippingDto;

import java.util.List;

/**
 * 物流 RPC 接口
 */
public interface LogisticsApi {

    /**
     * 根据订单 ID 查询物流单列表
     */
    List<LogShippingDto> getByOrderId(Long orderId);
}
