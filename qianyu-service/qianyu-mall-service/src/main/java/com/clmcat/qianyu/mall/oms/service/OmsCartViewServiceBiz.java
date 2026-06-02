package com.clmcat.qianyu.mall.oms.service;

import com.clmcat.qianyu.mall.oms.model.dto.CartAddDTO;
import com.clmcat.qianyu.mall.oms.model.dto.CartDeleteDTO;
import com.clmcat.qianyu.mall.oms.model.dto.CartUpdateDTO;
import com.clmcat.qianyu.mall.oms.model.vo.CartListVO;

public interface OmsCartViewServiceBiz {

    Long addCart(Long userId, CartAddDTO dto);

    void updateCart(Long userId, CartUpdateDTO dto);

    void deleteCart(Long userId, CartDeleteDTO dto);

    CartListVO listCart(Long userId);

}