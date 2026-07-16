package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.cms.CmsZoneRuleApi;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsZoneRuleDto;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminZoneIdDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营-内容管理：楼层投放规则（cms_zone_rule）CRUD。复用 cms:zone:manage 权限码。
 */
@Tag(name = "运营-内容管理", description = "楼层投放规则")
@ApiController
@RequestMapping("/api/admin/cms/zone-rule")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class CmsAdminZoneRuleController {

    @DubboReference
    private CmsZoneRuleApi cmsZoneRuleApi;

    @Operation(summary = "某楼层的规则列表")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/list")
    public List<CmsZoneRuleDto> list(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        return cmsZoneRuleApi.listByZone(dto.getId());
    }

    @Operation(summary = "新建规则")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/create")
    public Long create(@Token Long adminId, @Params CmsZoneRuleDto dto) {
        return cmsZoneRuleApi.create(dto);
    }

    @Operation(summary = "更新规则")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/update")
    public void update(@Token Long adminId, @Params CmsZoneRuleDto dto) {
        cmsZoneRuleApi.update(dto);
    }

    @Operation(summary = "删除规则")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/delete")
    public void delete(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        cmsZoneRuleApi.delete(dto.getId());
    }

    @Operation(summary = "切换规则启停")
    @RequiresPermission("cms:zone:manage")
    @PostMapping("/setStatus")
    public void setStatus(@Token Long adminId, @Params AdminZoneIdDTO dto) {
        cmsZoneRuleApi.setStatus(dto.getId(), dto.getStatus());
    }
}
