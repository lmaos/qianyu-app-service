package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.cms.CmsHomeTabApi;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsHomeTabDto;
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
 * 运营-内容管理：首页导航 Tab CRUD + 设默认。
 */
@Tag(name = "运营-内容管理", description = "导航 Tab")
@ApiController
@RequestMapping("/api/admin/cms/tab")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class CmsAdminTabController {

    @DubboReference
    private CmsHomeTabApi cmsHomeTabApi;

    @Operation(summary = "Tab 分页")
    @RequiresPermission("cms:tab:manage")
    @PostMapping("/page")
    public PageResultDTO<CmsHomeTabDto> page(@Token Long adminId, @Params AdminZonePageDTO dto) {
        return cmsHomeTabApi.page(
                dto != null ? dto.getKeyword() : null,
                dto != null ? dto.getStatus() : null,
                dto != null && dto.getPageNum() != null ? dto.getPageNum() : 1,
                dto != null && dto.getPageSize() != null ? dto.getPageSize() : 10);
    }

    @Operation(summary = "新建 Tab")
    @RequiresPermission("cms:tab:manage")
    @PostMapping("/create")
    public Long create(@Token Long adminId, @Params CmsHomeTabDto dto) {
        return cmsHomeTabApi.create(dto);
    }

    @Operation(summary = "更新 Tab")
    @RequiresPermission("cms:tab:manage")
    @PostMapping("/update")
    public void update(@Token Long adminId, @Params CmsHomeTabDto dto) {
        cmsHomeTabApi.update(dto);
    }

    @Operation(summary = "删除 Tab")
    @RequiresPermission("cms:tab:manage")
    @PostMapping("/delete")
    public void delete(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        cmsHomeTabApi.delete(dto.getId());
    }

    @Operation(summary = "设为默认 Tab")
    @RequiresPermission("cms:tab:manage")
    @PostMapping("/setDefault")
    public void setDefault(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        cmsHomeTabApi.setDefault(dto.getId());
    }
}
