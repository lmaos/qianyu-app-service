package com.clmcat.qianyu.mall.api.inv;

public interface InvStockLogApi {

    void addLog(Long skuId, Integer type, Integer quantity, Long orderId, String reason);
}
