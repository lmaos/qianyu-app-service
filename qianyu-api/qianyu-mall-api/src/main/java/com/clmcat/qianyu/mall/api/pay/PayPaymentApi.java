package com.clmcat.qianyu.mall.api.pay;

import com.clmcat.qianyu.mall.api.pay.model.dto.PayPaymentDto;

public interface PayPaymentApi {

    PayPaymentDto findByPaymentNo(String paymentNo);

    PayPaymentDto findLatestByOrderId(Long orderId);

    PayPaymentDto findPendingByOrderId(Long orderId);

    /**
     * S6 新增：CAS 关闭订单的 PENDING 支付单（PENDING→CLOSED）。
     * <p>订单超时取消/用户取消时调用，使悬挂的待支付单失效（重复 CAS 无害，affected=0 跳过）。
     *
     * @param orderId 订单 ID
     * @return 受影响行数（0 表示无 PENDING 单可关）
     */
    int closePendingByOrderId(Long orderId);
}
