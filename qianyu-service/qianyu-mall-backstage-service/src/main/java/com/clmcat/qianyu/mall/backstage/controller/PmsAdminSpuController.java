package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.pms.PmsSpuApi;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminSpuAuditDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminSpuListOffDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "运营-商品管理", description = "跨店分页/强制下架/审核")
@ApiController
@RequestMapping("/api/admin/spu")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class PmsAdminSpuController {

    @DubboReference
    private PmsSpuApi pmsSpuApi;

    @Operation(summary = "商品列表分页（跨店）")
    @RequiresPermission("pms:spu:view")
    @PostMapping("/page")
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto> page(
            @Token Long adminId,
            @Params com.clmcat.qianyu.mall.api.pms.model.dto.SpuPageQueryDTO dto) {
        return pmsSpuApi.pageByPlatform(dto);
    }

    @Operation(summary = "强制下架")
    @RequiresPermission("pms:spu:listoff")
    @PostMapping("/adminListOff")
    public void adminListOff(@Token Long adminId, @Params AdminSpuListOffDTO dto) {
        pmsSpuApi.adminListOff(dto.getSpuId(), dto.getReason());
    }

    @Operation(summary = "审核商品")
    @RequiresPermission("pms:spu:audit")
    @PostMapping("/audit")
    public void audit(@Token Long adminId, @Params AdminSpuAuditDTO dto) {
        pmsSpuApi.audit(dto.getSpuId(), dto.getApproved(), dto.getRejectReason());
    }
}
