package com.clmcat.qianyu.mall.coupon.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.coupon.SmsCouponApi;
import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponDto;
import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponPageDTO;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "商户优惠券管理")
@ApiController
@RequestMapping("/api/mall/merchant/coupon")
@LoginVerify
public class MerchantCouponController {

    @DubboReference
    private SmsCouponApi couponApi;

    @DubboReference
    private MerchantApi merchantApi;

    @Operation(summary = "商户优惠券列表")
    @PostMapping("/couponList")
    public PageResultDTO<CouponDto> couponList(
            @Parameter(hidden = true) @Token long userId,
            @Params CouponPageDTO dto) {
        Long merchantId = merchantApi.requireActiveMerchant(userId).getId();
        if (dto == null) dto = new CouponPageDTO();
        dto.setMerchantId(merchantId);
        return couponApi.pageCoupons(dto);
    }

    @Operation(summary = "优惠券详情")
    @PostMapping("/couponDetail")
    public CouponDto couponDetail(
            @Parameter(hidden = true) @Token long userId,
            @Params CouponDto dto) {
        merchantApi.requireActiveMerchant(userId);
        return couponApi.getCouponById(dto.getId());
    }

    @Operation(summary = "创建优惠券")
    @PostMapping("/couponCreate")
    public Long couponCreate(
            @Parameter(hidden = true) @Token long userId,
            @Params CouponDto dto) {
        Long merchantId = merchantApi.requireActiveMerchant(userId).getId();
        dto.setMerchantId(merchantId);
        return couponApi.createCoupon(dto);
    }

    @Operation(summary = "更新优惠券")
    @PostMapping("/couponUpdate")
    public void couponUpdate(
            @Parameter(hidden = true) @Token long userId,
            @Params CouponDto dto) {
        Long merchantId = merchantApi.requireActiveMerchant(userId).getId();
        dto.setMerchantId(merchantId);
        couponApi.updateCoupon(dto);
    }

    @Operation(summary = "删除优惠券")
    @PostMapping("/couponDelete")
    public void couponDelete(
            @Parameter(hidden = true) @Token long userId,
            @Params CouponDto dto) {
        merchantApi.requireActiveMerchant(userId);
        couponApi.deleteCoupon(dto.getId());
    }
}
