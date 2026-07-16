package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.cms.CmsZoneApi;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsZoneDto;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsZoneProductDto;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminZoneIdDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminZonePageDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminZoneProductAddDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminZoneProductUpdateDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营-内容管理：CMS 楼层（zone）CRUD + 楼层商品（手动选品）管理。
 */
@Tag(name = "运营-内容管理", description = "楼层/楼层商品")
@ApiController
@RequestMapping("/api/admin/cms/zone")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class CmsAdminZoneController {

    @DubboReference
    private CmsZoneApi cmsZoneApi;

    // ==================== 楼层 ====================

    @Operation(summary = "楼层分页")
    @RequiresPermission("cms:zone:view")
    @PostMapping("/page")
    public PageResultDTO<CmsZoneDto> page(@Token Long adminId, @Params AdminZonePageDTO dto) {
        return cmsZoneApi.zonePage(
                dto != null ? dto.getKeyword() : null,
                dto != null ? dto.getStatus() : null,
                dto != null && dto.getPageNum() != null ? dto.getPageNum() : 1,
                dto != null && dto.getPageSize() != null ? dto.getPageSize() : 10);
    }

    @Operation(summary = "新建楼层")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/create")
    public Long create(@Token Long adminId, @Params CmsZoneDto dto) {
        return cmsZoneApi.zoneCreate(dto);
    }

    @Operation(summary = "更新楼层")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/update")
    public void update(@Token Long adminId, @Params CmsZoneDto dto) {
        cmsZoneApi.zoneUpdate(dto);
    }

    @Operation(summary = "删除楼层")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/delete")
    public void delete(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        cmsZoneApi.zoneDelete(dto.getId());
    }

    @Operation(summary = "切换楼层显隐")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/setStatus")
    public void setStatus(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        cmsZoneApi.zoneSetStatus(dto.getId(), dto.getStatus());
    }

    // ==================== 楼层商品（手动选品）====================

    @Operation(summary = "楼层商品列表")
    @RequiresPermission("cms:zone:view")
    @PostMapping("/product/list")
    public List<CmsZoneProductDto> productList(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        return cmsZoneApi.zoneProductList(dto.getId());
    }

    @Operation(summary = "楼层加商品")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/product/add")
    public void productAdd(@Token Long adminId, @Params AdminZoneProductAddDTO dto) {
        cmsZoneApi.zoneProductAdd(dto.getZoneId(), dto.getSpuId(), dto.getSort());
    }

    @Operation(summary = "更新楼层商品（排序/显隐）")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/product/update")
    public void productUpdate(@Token Long adminId, @Params AdminZoneProductUpdateDTO dto) {
        cmsZoneApi.zoneProductUpdate(dto.getId(), dto.getSort(), dto.getStatus());
    }

    @Operation(summary = "移除楼层商品")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/product/remove")
    public void productRemove(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        cmsZoneApi.zoneProductRemove(dto.getId());
    }
}
