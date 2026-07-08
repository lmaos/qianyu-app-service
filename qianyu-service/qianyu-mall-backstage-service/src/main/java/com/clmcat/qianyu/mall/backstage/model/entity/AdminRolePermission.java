package com.clmcat.qianyu.mall.backstage.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 角色-权限关联（t_admin_role_permission，复合主键 role_id+permission_id，无 deleted）。
 * @Id 取 roleId 仅为 MyBatis-Flex BaseMapper 主键要求；实际唯一性由复合键 + 业务保证。
 */
@Data
@Table("t_admin_role_permission")
public class AdminRolePermission {
    @Id(keyType = KeyType.None)
    @Column("role_id")
    private Long roleId;
    @Column("permission_id") private Long permissionId;
    @Column("create_time") private Long createTime;
}
