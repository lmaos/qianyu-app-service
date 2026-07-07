package com.clmcat.qianyu.mall.ads.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.ads.model.dto.AddressCreateDTO;
import com.clmcat.qianyu.mall.ads.model.dto.AddressIdDTO;
import com.clmcat.qianyu.mall.ads.model.dto.AddressUpdateDTO;
import com.clmcat.qianyu.mall.ads.model.vo.AddressItemVO;
import com.clmcat.qianyu.mall.ads.service.AdsAddressViewBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "收货地址", description = "用户收货地址管理")
@ApiController
@RequestMapping("/api/mall/ads")
@LoginVerify
public class AdsAddressController {

    @Resource
    private AdsAddressViewBiz addressViewBiz;

    @Operation(summary = "收货地址列表")
    @PostMapping("/addressList")
    public List<AddressItemVO> addressList(@Parameter(hidden = true) @Token long userId) {
        return addressViewBiz.getAddressList(userId);
    }

    @Operation(summary = "收货地址详情")
    @PostMapping("/addressDetail")
    public AddressItemVO addressDetail(
            @Parameter(hidden = true) @Token long userId,
            @Params AddressIdDTO dto) {
        return addressViewBiz.getAddressDetail(userId, dto.getAddressId());
    }

    @Operation(summary = "新增收货地址")
    @PostMapping("/addressCreate")
    public Long addressCreate(
            @Parameter(hidden = true) @Token long userId,
            @Params AddressCreateDTO dto) {
        return addressViewBiz.createAddress(userId, dto);
    }

    @Operation(summary = "更新收货地址")
    @PostMapping("/addressUpdate")
    public void addressUpdate(
            @Parameter(hidden = true) @Token long userId,
            @Params AddressUpdateDTO dto) {
        addressViewBiz.updateAddress(userId, dto);
    }

    @Operation(summary = "删除收货地址")
    @DeleteMapping("/addressDelete")
    public void addressDelete(
            @Parameter(hidden = true) @Token long userId,
            @Params AddressIdDTO dto) {
        addressViewBiz.deleteAddress(userId, dto.getAddressId());
    }

    @Operation(summary = "设置默认地址")
    @PostMapping("/addressSetDefault")
    public void addressSetDefault(
            @Parameter(hidden = true) @Token long userId,
            @Params AddressIdDTO dto) {
        addressViewBiz.setDefault(userId, dto.getAddressId());
    }
}
