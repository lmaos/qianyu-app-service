package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 给运营角色分配权限请求（/api/admin/role/assignPermissions）。
 * <p>Biz 层先 deleteByQuery(role_id) 清旧关联，再批量 insertSelective 新关联（先删后插，幂等）。
 */
@Data
public class AdminRoleAssignPermissionsDTO {
    /** 角色 ID（雪花）。 */
    private Long roleId;
    /** 权限 ID 列表（全量覆盖；空列表=清空权限）。 */
    private List<Long> permissionIds;
}
