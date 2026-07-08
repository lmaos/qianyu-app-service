package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 运营角色创建请求（/api/admin/role/create）。
 * <p>role_code 唯一（业务层校验），permCode 字典 M0 冻结的角色载体。
 */
@Data
public class AdminRoleCreateDTO {
    /** 角色码（唯一）。 */
    private String roleCode;
    /** 角色名。 */
    private String roleName;
    /** 备注。 */
    private String remark;
}
