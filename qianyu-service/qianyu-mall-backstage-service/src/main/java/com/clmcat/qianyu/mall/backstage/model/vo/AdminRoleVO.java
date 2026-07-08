package com.clmcat.qianyu.mall.backstage.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 运营角色列表项 VO（/api/admin/role/page、/api/admin/account/roles）。
 * <p>permissionIds 由 Biz 层富化：role → role_permission 聚合 permission_id。
 */
@Data
@Builder
public class AdminRoleVO {
    /** 角色 ID（雪花）。 */
    private Long id;
    /** 角色码。 */
    private String roleCode;
    /** 角色名。 */
    private String roleName;
    /** 状态：1启用/0禁用。 */
    private Integer status;
    /** 备注。 */
    private String remark;
    /** 创建时间（毫秒戳）。 */
    private Long createTime;
    /** 该角色绑定的权限 ID 列表（Biz 层富化）。 */
    private List<Long> permissionIds;
}
