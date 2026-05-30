package com.clmcat.qianyu.mall.api.oms;

import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;

public interface OmsOrderApi {

    OmsOrderDto findById(Long orderId);

    OmsOrderDto findByOrderNo(String orderNo);

    boolean updateWithOptimisticLock(OmsOrderDto order);

    boolean transitStatus(Long orderId, int fromStatus, int toStatus);
}
