package com.clmcat.qianyu.mall.mch.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.mch.model.dto.MerchantAuditDTO;
import com.clmcat.qianyu.mall.mch.model.dto.MerchantSettleDTO;
import com.clmcat.qianyu.mall.mch.model.dto.ShopInfoUpdateDTO;
import com.clmcat.qianyu.mall.mch.model.vo.MerchantDashboardVO;
import com.clmcat.qianyu.mall.mch.model.vo.SettleResultVO;
import com.clmcat.qianyu.mall.mch.model.vo.ShopInfoVO;
import com.clmcat.qianyu.mall.mch.service.MerchantViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "商家管理", description = "商家入驻、店铺、审核")
@ApiController
@RequestMapping("/api/mall/merchant/merchant")
@LoginVerify
public class MerchantMerchantController {

    @Resource
    private MerchantViewServiceBiz merchantViewServiceBiz;

    // app.md §12 /api/mall/merchant/merchant/dashboard
    /**
     * 商家管理首页
     */
    @Operation(summary = "商家管理首页")
    @PostMapping("/dashboard")
    public MerchantDashboardVO dashboard(@Parameter(hidden = true) @Token long userId) {
        return merchantViewServiceBiz.getDashboard(userId);
    }

    // app.md §12.2 /api/mall/merchant/merchant/settleApply
    /**
     * 商家入驻申请
     */
    @Operation(summary = "商家入驻申请")
    @PostMapping("/settleApply")
    public SettleResultVO settleApply(
            @Parameter(hidden = true) @Token long userId,
            @Params MerchantSettleDTO dto) {
        return merchantViewServiceBiz.settleApply(userId, dto);
    }

    // app.md §12.2 /api/mall/merchant/merchant/settleResult
    /**
     * 入驻审核结果查询
     */
    @Operation(summary = "入驻审核结果查询")
    @PostMapping("/settleResult")
    public SettleResultVO settleResult(@Parameter(hidden = true) @Token long userId) {
        return merchantViewServiceBiz.getSettleResult(userId);
    }

    // app.md §12.2 /api/mall/merchant/merchant/shopInfo
    /**
     * 店铺信息查询
     */
    @Operation(summary = "店铺信息查询")
    @PostMapping("/shopInfo")
    public ShopInfoVO shopInfo(@Parameter(hidden = true) @Token long userId) {
        return merchantViewServiceBiz.getShopInfo(userId);
    }

    // app.md §12.2 /api/mall/merchant/merchant/shopInfoUpdate
    /**
     * 店铺信息更新
     */
    @Operation(summary = "店铺信息更新")
    @PostMapping("/shopInfoUpdate")
    public void shopInfoUpdate(
            @Parameter(hidden = true) @Token long userId,
            @Params ShopInfoUpdateDTO dto) {
        merchantViewServiceBiz.updateShopInfo(userId, dto);
    }

    // app.md §12.2 /api/mall/merchant/merchant/settleAudit
    /**
     * 平台管理员 - 审核商家入驻
     */
    @Operation(summary = "审核商家入驻")
    @PostMapping("/settleAudit")
    public void settleAudit(
            @Parameter(hidden = true) @Token long userId,
            @Params MerchantAuditDTO dto) {
        merchantViewServiceBiz.auditMerchant(userId, dto);
    }

    /**
     * 商家详情
     */
    @Operation(summary = "商家详情")
    @PostMapping("/merchantDetail")
    public ShopInfoVO merchantDetail(@Parameter(hidden = true) @Token long userId) {
        return merchantViewServiceBiz.getShopInfo(userId);
    }
}
