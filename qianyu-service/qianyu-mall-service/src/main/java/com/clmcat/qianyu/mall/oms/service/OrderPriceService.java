package com.clmcat.qianyu.mall.oms.service;

import com.clmcat.qianyu.mall.oms.model.vo.FreightContext;
import com.clmcat.qianyu.mall.oms.model.vo.PriceResult;

import java.math.BigDecimal;

public interface OrderPriceService {

    /** 旧签名（freight=0），向后兼容 */
    PriceResult calculatePrice(long userId, BigDecimal totalAmount, Long userCouponId);

    /** Phase2：含运费计算的完整签名 */
    PriceResult calculatePrice(long userId, BigDecimal totalAmount, Long userCouponId, FreightContext freightCtx);
}
