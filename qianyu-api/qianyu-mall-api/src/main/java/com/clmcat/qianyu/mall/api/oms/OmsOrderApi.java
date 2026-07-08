package com.clmcat.qianyu.mall.api.oms;

import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.api.oms.model.dto.OrderPageQueryDTO;

import java.util.List;

public interface OmsOrderApi {

    OmsOrderDto findById(Long orderId);

    OmsOrderDto findByOrderNo(String orderNo);

    boolean updateWithOptimisticLock(OmsOrderDto order);

    boolean transitStatus(Long orderId, int fromStatus, int toStatus);

    /**
     * 运营端订单跨店分页查询。
     * <p>按 merchantId/status/orderNo(模糊)/buyerUserId 过滤，按 create_time DESC 排序，
     * 返回当前页 OmsOrderDto 列表（不含明细行）。
     *
     * @param query 分页与过滤条件（pageNum/pageSize 缺省 1/10）
     * @return 当前页订单 DTO 列表；无数据返回空列表
     */
    List<OmsOrderDto> pageByPlatform(OrderPageQueryDTO query);
}
