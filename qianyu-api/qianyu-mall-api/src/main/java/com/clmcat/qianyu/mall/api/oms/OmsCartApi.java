package com.clmcat.qianyu.mall.api.oms;

import com.clmcat.qianyu.mall.api.oms.model.dto.OmsCartDto;

import java.util.List;

public interface OmsCartApi {

    OmsCartDto findByUserAndSku(Long userId, Long skuId);

    void insert(OmsCartDto cart);

    void update(OmsCartDto cart);

    List<OmsCartDto> listByUserId(Long userId);

    void deleteBatch(List<Long> cartItemIds);
}
