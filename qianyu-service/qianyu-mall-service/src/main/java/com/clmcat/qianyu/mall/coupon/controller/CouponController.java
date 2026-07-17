package com.clmcat.qianyu.mall.coupon.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponDto;
import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponPageDTO;
import com.clmcat.qianyu.mall.coupon.model.dto.CouponUsableDTO;
import com.clmcat.qianyu.mall.coupon.model.vo.CouponCardVO;
import com.clmcat.qianyu.mall.coupon.model.vo.UsableCouponVO;
import com.clmcat.qianyu.mall.coupon.model.vo.UserCouponVO;
import com.clmcat.qianyu.mall.coupon.service.CouponViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "优惠券(C端)")
@ApiController
@RequestMapping("/api/mall/coupon")
@LoginVerify
public class CouponController {

    @Resource
    private CouponViewServiceBiz couponViewServiceBiz;

    @Operation(summary = "可领优惠券列表")
    @PostMapping("/claimable")
    public List<CouponCardVO> claimable(
            @Parameter(hidden = true) @Token long userId,
            @Params CouponDto dto) {
        Long merchantId = dto != null ? dto.getMerchantId() : null;
        return couponViewServiceBiz.claimableCoupons(userId, merchantId);
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/claim")
    public Long claim(
            @Parameter(hidden = true) @Token long userId,
            @Params CouponDto dto) {
        return couponViewServiceBiz.claim(userId, dto.getId());
    }

    @Operation(summary = "我的优惠券列表")
    @PostMapping("/myList")
    public Page<UserCouponVO> myList(
            @Parameter(hidden = true) @Token long userId,
            @Params CouponPageDTO dto) {
        int status = dto != null && dto.getStatus() != null ? dto.getStatus() : 0;
        int pageNum = dto != null && dto.getPageNum() != null ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null ? dto.getPageSize() : 10;
        return couponViewServiceBiz.myCoupons(userId, status, pageNum, pageSize);
    }

    @Operation(summary = "下单可用券（含试算抵扣）")
    @PostMapping("/usable")
    public List<UsableCouponVO> usable(
            @Parameter(hidden = true) @Token long userId,
            @Params CouponUsableDTO dto) {
        java.math.BigDecimal amount = dto != null && dto.getOrderAmount() != null
                ? dto.getOrderAmount() : java.math.BigDecimal.ZERO;
        return couponViewServiceBiz.usableForOrder(userId, amount);
    }
}
