package com.clmcat.qianyu.mall.api.oms;

import com.clmcat.qianyu.mall.api.oms.model.dto.OmsAfterSaleDto;

public interface OmsAfterSaleApi {

    void insert(OmsAfterSaleDto afterSale);

    OmsAfterSaleDto findById(Long aftersaleId);

    void update(OmsAfterSaleDto afterSale);
}
