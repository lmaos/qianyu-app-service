package com.clmcat.qianyu.mall.backstage.service;

import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRoleAssignPermissionsDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRoleCreateDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRolePageQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRoleUpdateDTO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminRoleVO;

import java.util.List;

/**
 * 运营角色管理服务（CRUD + 权限分配）。
 * <p>面向「运营-角色管理」管理后台页面，需 admin:role:manage 权限。
 */
public interface AdminRoleViewServiceBiz {

    /**
     * 角色分页查询（带 permissionIds 富化）。
     * @return 分页结果（records 为 AdminRoleVO，含 total/页码）
     */
    PageResultDTO<AdminRoleVO> page(AdminRolePageQueryDTO dto);

    /**
     * 创建角色：role_code 唯一性校验。
     * @return 新角色 ID（雪花）
     */
    Long create(AdminRoleCreateDTO dto);

    /**
     * 更新角色（role_name / remark；role_code 不动）。
     */
    void update(AdminRoleUpdateDTO dto);

    /**
     * 查询某角色绑定的权限 ID 列表（/api/admin/role/permissions）。
     */
    List<Long> getPermissions(Long roleId);

    /**
     * 给角色分配权限（先 deleteByQuery role_id 再批量 insertSelective；幂等全量覆盖）。
     */
    void assignPermissions(AdminRoleAssignPermissionsDTO dto);
}
