package com.clmcat.qianyu.mall.api.pay;

import com.clmcat.qianyu.mall.api.pay.model.dto.PayPaymentDto;

public interface PayPaymentApi {

    PayPaymentDto findByPaymentNo(String paymentNo);

    PayPaymentDto findLatestByOrderId(Long orderId);

    PayPaymentDto findPendingByOrderId(Long orderId);
}
