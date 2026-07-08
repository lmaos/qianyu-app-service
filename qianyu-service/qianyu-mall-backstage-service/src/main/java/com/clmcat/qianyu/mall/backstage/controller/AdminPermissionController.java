package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminPermissionTreeNodeVO;
import com.clmcat.qianyu.mall.backstage.service.AdminPermissionViewServiceBiz;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营-权限管理 Controller（权限树）。
 * <p>类级 {@code @LoginVerify(BackstageLoginVerifyFunction.class, token="X-Admin-Token")}；
 * 方法 {@code @RequiresPermission("admin:permission:manage")}。
 */
@Tag(name = "运营-权限管理", description = "权限树查询")
@ApiController
@RequestMapping("/api/admin/permission")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class AdminPermissionController {

    @Resource
    private AdminPermissionViewServiceBiz permissionService;

    @Operation(summary = "权限树（按 parentId 递归构建，根 parentId=0）")
    @RequiresPermission("admin:permission:manage")
    @PostMapping("/tree")
    public List<AdminPermissionTreeNodeVO> tree(@Token Long adminId) {
        return permissionService.tree();
    }
}
