package com.clmcat.qianyu.mall.api.coupon;

import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponDto;
import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponPageDTO;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;

import java.util.List;

/**
 * 优惠券模板 RPC 契约（后台/商户 CRUD + C 端查询）。
 */
public interface SmsCouponApi {

    CouponDto getCouponById(Long id);

    /** 分页查询（merchantId=0 平台券，>0 商家券） */
    PageResultDTO<CouponDto> pageCoupons(CouponPageDTO query);

    /** 创建模板（返回 id） */
    Long createCoupon(CouponDto dto);

    void updateCoupon(CouponDto dto);

    void deleteCoupon(Long id);

    /** 启用/禁用（0=禁 1=启） */
    void setCouponStatus(Long id, int status);

    /** C 端领券中心：查询可领券列表 */
    List<CouponDto> listClaimable(Long merchantId);

    // —— P3 核销原语（PriceService 调用）——

    /** 核销：CAS status 1→2 + set orderId/useTime。返回 true 成功。 */
    boolean lockAndApplyCoupon(Long userCouponId, Long orderId);

    /** 回滚：CAS status 2→1 + clear orderId。 */
    boolean rollbackCoupon(Long userCouponId);
}
