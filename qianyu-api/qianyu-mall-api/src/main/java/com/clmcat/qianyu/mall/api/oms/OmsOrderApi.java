package com.clmcat.qianyu.mall.api.oms;

import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderItemDto;
import com.clmcat.qianyu.mall.api.oms.model.dto.OrderPageQueryDTO;

import java.util.List;

public interface OmsOrderApi {

    OmsOrderDto findById(Long orderId);

    OmsOrderDto findByOrderNo(String orderNo);

    boolean updateWithOptimisticLock(OmsOrderDto order);

    boolean transitStatus(Long orderId, int fromStatus, int toStatus);

    /**
     * S9: 支付成功专用——CAS 推进订单 10(待付款)→20(待发货) 并回写 pay_time。
     * <p>比 {@link #transitStatus} 多 set pay_time，语义精确匹配支付成功场景。
     *
     * @param orderId 订单 ID
     * @return true 成功；false 表示订单状态非 10（并发取消/已支付），调用方须补偿退款
     */
    boolean markPaid(Long orderId);

    /**
     * S22: 自动确认收货专用——CAS 推进订单 30(待收货)→40(已完成) 并回写 receive_time。
     *
     * @param orderId 订单 ID
     * @return true 成功；false 表示订单状态非 30（已确认/已取消等）
     */
    boolean markReceived(Long orderId);

    /**
     * 发货专用——CAS 推进订单 20(待发货)→30(待收货) 并回写 delivery_time。
     * <p>真 CAS（WHERE id + status + version），防并发双推进。供 shipOrder 调用。
     *
     * @param orderId 订单 ID
     * @return true 成功；false 表示订单状态非 20（已发货/已取消等并发变更）
     */
    boolean markShipped(Long orderId);

    /**
     * 按订单 ID 查明细（S4 新增契约）。供 InvStockApi.confirmStock 跨域取 skuId+quantity 核销库存。
     *
     * @param orderId 订单 ID
     * @return 订单明细 DTO 列表（无明细返回空列表）
     */
    List<OmsOrderItemDto> findOrderItemsByOrderId(Long orderId);

    /**
     * 运营端订单跨店分页查询。
     * <p>按 merchantId/status/orderNo(模糊)/buyerUserId 过滤，按 create_time DESC 排序，
     * 返回当前页 OmsOrderDto 列表（不含明细行）。
     *
     * @param query 分页与过滤条件（pageNum/pageSize 缺省 1/10）
     * @return 分页结果（含当前页 records + total/页码）；无数据 records 为空列表
     */
    PageResultDTO<OmsOrderDto> pageByPlatform(OrderPageQueryDTO query);
}
