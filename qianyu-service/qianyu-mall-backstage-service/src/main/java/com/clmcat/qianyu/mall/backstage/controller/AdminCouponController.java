package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.coupon.SmsCouponApi;
import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponDto;
import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponPageDTO;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "后台-优惠券管理")
@ApiController
@RequestMapping("/api/admin/coupon")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class AdminCouponController {

    @DubboReference
    private SmsCouponApi couponApi;

    @Operation(summary = "优惠券分页")
    @PostMapping("/page")
    @RequiresPermission("sms:coupon:view")
    public PageResultDTO<CouponDto> page(@Parameter(hidden = true) @Token Long adminId,
                                         @Params CouponPageDTO dto) {
        // 后台默认查平台券（merchantId=0），可按 DTO 传入查指定商家
        if (dto != null && dto.getMerchantId() == null) {
            dto.setMerchantId(0L);
        }
        return couponApi.pageCoupons(dto);
    }

    @Operation(summary = "创建平台优惠券")
    @PostMapping("/create")
    @RequiresPermission("sms:coupon:create")
    public Long create(@Parameter(hidden = true) @Token Long adminId,
                       @Params CouponDto dto) {
        dto.setMerchantId(0L); // 平台券
        return couponApi.createCoupon(dto);
    }

    @Operation(summary = "更新优惠券")
    @PostMapping("/update")
    @RequiresPermission("sms:coupon:update")
    public void update(@Parameter(hidden = true) @Token Long adminId,
                       @Params CouponDto dto) {
        couponApi.updateCoupon(dto);
    }

    @Operation(summary = "删除优惠券")
    @PostMapping("/delete")
    @RequiresPermission("sms:coupon:delete")
    public void delete(@Parameter(hidden = true) @Token Long adminId,
                       @Params CouponDto dto) {
        couponApi.deleteCoupon(dto.getId());
    }

    @Operation(summary = "启用/禁用")
    @PostMapping("/setStatus")
    @RequiresPermission("sms:coupon:manage")
    public void setStatus(@Parameter(hidden = true) @Token Long adminId,
                          @Params CouponDto dto) {
        couponApi.setCouponStatus(dto.getId(), dto.getStatus());
    }
}
