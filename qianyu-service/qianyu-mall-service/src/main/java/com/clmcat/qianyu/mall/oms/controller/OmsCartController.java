package com.clmcat.qianyu.mall.oms.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.oms.model.dto.CartAddDTO;
import com.clmcat.qianyu.mall.oms.model.dto.CartDeleteDTO;
import com.clmcat.qianyu.mall.oms.model.dto.CartUpdateDTO;
import com.clmcat.qianyu.mall.oms.model.vo.CartListVO;
import com.clmcat.qianyu.mall.oms.service.OmsCartViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "OMS-购物车")
@ApiController
@RequestMapping("/api/mall/oms")
// @LoginVerify
public class OmsCartController {

    @Resource
    private OmsCartViewServiceBiz cartViewServiceBiz;

    // app.md §8.6 /api/mall/oms/cartAdd
    @Operation(summary = "加入购物车")
    @PostMapping("/cartAdd")
    public Long cartAdd(@Parameter(hidden = true) @Token long userId, @Params CartAddDTO dto) {
        return cartViewServiceBiz.addCart(userId, dto);
    }

    @Operation(summary = "更新购物车数量")
    @PostMapping("/cartUpdate")
    public void cartUpdate(@Parameter(hidden = true) @Token long userId, @Params CartUpdateDTO dto) {
        cartViewServiceBiz.updateCart(userId, dto);
    }

    @Operation(summary = "删除购物车")
    @PostMapping("/cartDelete")
    public void cartDelete(@Parameter(hidden = true) @Token long userId, @Params CartDeleteDTO dto) {
        cartViewServiceBiz.deleteCart(userId, dto);
    }

    // app.md §8.7 /api/mall/oms/cartList
    @Operation(summary = "购物车列表")
    @PostMapping("/cartList")
    public CartListVO cartList(@Parameter(hidden = true) @Token long userId) {
        return cartViewServiceBiz.listCart(userId);
    }
}
