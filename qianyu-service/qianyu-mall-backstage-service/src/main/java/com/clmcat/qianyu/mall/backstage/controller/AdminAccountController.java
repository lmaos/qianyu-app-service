package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountAssignRolesDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountCreateDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountPageQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountUpdateDTO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminAccountVO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminRoleVO;
import com.clmcat.qianyu.mall.backstage.service.AdminAccountAdminServiceBiz;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营-账号管理 Controller（CRUD + 角色分配）。
 * <p>类级 {@code @LoginVerify(BackstageLoginVerifyFunction.class, token="X-Admin-Token")}（X-Admin-Token header）；
 * 全部方法 {@code @RequiresPermission("admin:account:manage")}（permCode 字典 M0 冻结）。
 *
 * <p>区别于 {@link BackstageAccountController}（登录/登出/账号信息），本 Controller 面向
 * 「运营-账号管理」管理后台页面（admin:account:manage 权限）。
 */
@Tag(name = "运营-账号管理", description = "账号CRUD/禁用/重置密码/角色分配")
@ApiController
@RequestMapping("/api/admin/account")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class AdminAccountController {

    @Resource
    private AdminAccountAdminServiceBiz accountAdminService;

    @Operation(summary = "账号分页（带 roleNames 富化）")
    @RequiresPermission("admin:account:manage")
    @PostMapping("/page")
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<AdminAccountVO> page(
            @Token Long adminId, @Params AdminAccountPageQueryDTO dto) {
        return accountAdminService.page(dto);
    }

    @Operation(summary = "创建账号（BCrypt 哈希密码）")
    @RequiresPermission("admin:account:manage")
    @PostMapping("/create")
    public Long create(@Token Long adminId, @Params AdminAccountCreateDTO dto) {
        return accountAdminService.create(dto);
    }

    @Operation(summary = "更新账号资料（realName/mobile/email）")
    @RequiresPermission("admin:account:manage")
    @PostMapping("/update")
    public void update(@Token Long adminId, @Params AdminAccountUpdateDTO dto) {
        accountAdminService.update(dto);
    }

    @Operation(summary = "禁用账号（status=0）")
    @RequiresPermission("admin:account:manage")
    @PostMapping("/disable")
    public void disable(@Token Long adminId, @Params AdminAccountUpdateDTO dto) {
        accountAdminService.disable(dto.getId());
    }

    @Operation(summary = "重置密码（BCrypt 哈希新密码）")
    @RequiresPermission("admin:account:manage")
    @PostMapping("/resetPwd")
    public void resetPwd(@Token Long adminId, @Params AdminAccountUpdateDTO dto) {
        accountAdminService.resetPwd(dto);
    }

    @Operation(summary = "查询账号已分配的角色列表")
    @RequiresPermission("admin:account:manage")
    @PostMapping("/roles")
    public List<AdminRoleVO> roles(@Token Long adminId, @Params AdminAccountUpdateDTO dto) {
        return accountAdminService.getRoles(dto.getId());
    }

    @Operation(summary = "给账号分配角色（先删后插，幂等全量覆盖）")
    @RequiresPermission("admin:account:manage")
    @PostMapping("/assignRoles")
    public void assignRoles(@Token Long adminId, @Params AdminAccountAssignRolesDTO dto) {
        accountAdminService.assignRoles(dto);
    }
}
