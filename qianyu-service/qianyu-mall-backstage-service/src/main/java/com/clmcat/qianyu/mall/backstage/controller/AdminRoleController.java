package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRoleAssignPermissionsDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRoleCreateDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRolePageQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRoleUpdateDTO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminRoleVO;
import com.clmcat.qianyu.mall.backstage.service.AdminRoleViewServiceBiz;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营-角色管理 Controller（CRUD + 权限分配）。
 * <p>类级 {@code @LoginVerify(BackstageLoginVerifyFunction.class, token="X-Admin-Token")}；
 * 全部方法 {@code @RequiresPermission("admin:role:manage")}。
 */
@Tag(name = "运营-角色管理", description = "角色CRUD/权限分配")
@ApiController
@RequestMapping("/api/admin/role")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class AdminRoleController {

    @Resource
    private AdminRoleViewServiceBiz roleService;

    @Operation(summary = "角色分页（带 permissionIds 富化）")
    @RequiresPermission("admin:role:manage")
    @PostMapping("/page")
    public List<AdminRoleVO> page(@Token Long adminId, @Params AdminRolePageQueryDTO dto) {
        return roleService.page(dto).getRecords();
    }

    @Operation(summary = "创建角色")
    @RequiresPermission("admin:role:manage")
    @PostMapping("/create")
    public Long create(@Token Long adminId, @Params AdminRoleCreateDTO dto) {
        return roleService.create(dto);
    }

    @Operation(summary = "更新角色（roleName/remark；roleCode 不动）")
    @RequiresPermission("admin:role:manage")
    @PostMapping("/update")
    public void update(@Token Long adminId, @Params AdminRoleUpdateDTO dto) {
        roleService.update(dto);
    }

    @Operation(summary = "查询角色绑定的权限ID列表")
    @RequiresPermission("admin:role:manage")
    @PostMapping("/permissions")
    public List<Long> permissions(@Token Long adminId, @Params AdminRoleUpdateDTO dto) {
        return roleService.getPermissions(dto.getId());
    }

    @Operation(summary = "给角色分配权限（先删后插，幂等全量覆盖）")
    @RequiresPermission("admin:role:manage")
    @PostMapping("/assignPermissions")
    public void assignPermissions(@Token Long adminId, @Params AdminRoleAssignPermissionsDTO dto) {
        roleService.assignPermissions(dto);
    }
}
