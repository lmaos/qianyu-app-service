package com.clmcat.qianyu.mall.api.pay;

import com.clmcat.qianyu.mall.api.pay.model.dto.PayRefundDto;

public interface PayRefundApi {

    void insert(PayRefundDto refund);

    PayRefundDto findByRefundNo(String refundNo);
}
