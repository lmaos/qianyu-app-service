package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.MerchantStoreApi;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminMerchantAuditDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminMerchantStatusDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 运营商户管理 Controller。
 * <p>类级 @LoginVerify(BackstageLoginVerifyFunction) + 方法 @RequiresPermission。
 * <p>audit 迁移自 settleAudit；page 跨店分页；freeze/unfreeze/disable 状态管控；store/info 店铺查看。
 */
@Tag(name = "运营-商户管理", description = "入驻审核/状态管控/店铺查看")
@ApiController
@RequestMapping("/api/admin/merchant")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class MchAdminMerchantController {

    @DubboReference
    private MerchantApi merchantApi;

    @DubboReference
    private MerchantStoreApi merchantStoreApi;

    @Operation(summary = "审核商家入驻")
    @RequiresPermission("mch:audit")
    @PostMapping("/audit")
    public void audit(@Token Long adminId, @Params AdminMerchantAuditDTO dto) {
        merchantApi.auditMerchant(dto.getMerchantId(), dto.getApproved(), dto.getRejectReason());
    }

    @Operation(summary = "商户列表分页")
    @RequiresPermission("mch:merchant:view")
    @PostMapping("/page")
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto> page(
            @Token Long adminId,
            @Params com.clmcat.qianyu.mall.api.mch.model.dto.MerchantPageQueryDTO dto) {
        return merchantApi.pageMerchants(dto);
    }

    @Operation(summary = "冻结商户")
    @RequiresPermission("mch:merchant:freeze")
    @PostMapping("/freeze")
    public void freeze(@Token Long adminId, @Params AdminMerchantStatusDTO dto) {
        merchantApi.updateMerchantStatus(dto.getMerchantId(), 2);
    }

    @Operation(summary = "解冻商户")
    @RequiresPermission("mch:merchant:freeze")
    @PostMapping("/unfreeze")
    public void unfreeze(@Token Long adminId, @Params AdminMerchantStatusDTO dto) {
        merchantApi.updateMerchantStatus(dto.getMerchantId(), 1);
    }

    @Operation(summary = "禁用商户")
    @RequiresPermission("mch:merchant:disable")
    @PostMapping("/disable")
    public void disable(@Token Long adminId, @Params AdminMerchantStatusDTO dto) {
        merchantApi.updateMerchantStatus(dto.getMerchantId(), 0);
    }

    @Operation(summary = "店铺信息")
    @RequiresPermission("mch:merchant:view")
    @PostMapping("/store/info")
    public com.clmcat.qianyu.mall.api.mch.model.dto.MerchantStoreDto storeInfo(
            @Token Long adminId, @Params AdminMerchantStatusDTO dto) {
        return merchantStoreApi.getByMerchantId(dto.getMerchantId());
    }
}
