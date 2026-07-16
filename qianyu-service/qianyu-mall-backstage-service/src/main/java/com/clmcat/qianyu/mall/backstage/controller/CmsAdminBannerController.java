package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.cms.CmsBannerApi;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsBannerDto;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminZoneIdDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminZonePageDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 运营-内容管理：首页轮播 Banner CRUD。
 */
@Tag(name = "运营-内容管理", description = "轮播 Banner")
@ApiController
@RequestMapping("/api/admin/cms/banner")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class CmsAdminBannerController {

    @DubboReference
    private CmsBannerApi cmsBannerApi;

    @Operation(summary = "Banner 分页")
    @RequiresPermission("cms:banner:manage")
    @PostMapping("/page")
    public PageResultDTO<CmsBannerDto> page(@Token Long adminId, @Params AdminZonePageDTO dto) {
        return cmsBannerApi.page(
                dto != null ? dto.getKeyword() : null,
                dto != null ? dto.getStatus() : null,
                dto != null && dto.getPageNum() != null ? dto.getPageNum() : 1,
                dto != null && dto.getPageSize() != null ? dto.getPageSize() : 10);
    }

    @Operation(summary = "新建 Banner")
    @RequiresPermission("cms:banner:manage")
    @PostMapping("/create")
    public Long create(@Token Long adminId, @Params CmsBannerDto dto) {
        return cmsBannerApi.create(dto);
    }

    @Operation(summary = "更新 Banner")
    @RequiresPermission("cms:banner:manage")
    @PostMapping("/update")
    public void update(@Token Long adminId, @Params CmsBannerDto dto) {
        cmsBannerApi.update(dto);
    }

    @Operation(summary = "删除 Banner")
    @RequiresPermission("cms:banner:manage")
    @PostMapping("/delete")
    public void delete(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        cmsBannerApi.delete(dto.getId());
    }
}
