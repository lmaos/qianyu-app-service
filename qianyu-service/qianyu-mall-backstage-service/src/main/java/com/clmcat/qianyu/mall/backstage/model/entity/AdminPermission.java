package com.clmcat.qianyu.mall.backstage.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/** 运营权限点（t_admin_permission）。type 1菜单/2按钮/3接口；perm_code M0 冻结字典（前后端 1:1）。 */
@Data
@Table("t_admin_permission")
public class AdminPermission {
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;
    @Column("perm_code") private String permCode;
    @Column("perm_name") private String permName;
    @Column("type") private Integer type;
    @Column("parent_id") private Long parentId;
    @Column("path") private String path;
    @Column("method") private String method;
    @Column("create_time") private Long createTime;
    @Column("update_time") private Long updateTime;
    @Column("deleted") private Integer deleted;
}
