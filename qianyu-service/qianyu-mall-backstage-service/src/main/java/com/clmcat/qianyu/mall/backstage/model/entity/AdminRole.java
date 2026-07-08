package com.clmcat.qianyu.mall.backstage.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/** 运营角色（t_admin_role）。 */
@Data
@Table("t_admin_role")
public class AdminRole {
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;
    @Column("role_code") private String roleCode;
    @Column("role_name") private String roleName;
    @Column("status") private Integer status;
    @Column("remark") private String remark;
    @Column("create_time") private Long createTime;
    @Column("update_time") private Long updateTime;
    @Column("deleted") private Integer deleted;
}
