package com.clmcat.qianyu.mall.backstage.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 账号-角色关联（t_admin_account_role，复合主键 account_id+role_id，无 deleted）。
 * @Id 取 accountId 仅为 MyBatis-Flex BaseMapper 主键要求；实际唯一性由复合键 + 业务保证。
 */
@Data
@Table("t_admin_account_role")
public class AdminAccountRole {
    @Id(keyType = KeyType.None)
    @Column("account_id")
    private Long accountId;
    @Column("role_id") private Long roleId;
    @Column("create_time") private Long createTime;
}
