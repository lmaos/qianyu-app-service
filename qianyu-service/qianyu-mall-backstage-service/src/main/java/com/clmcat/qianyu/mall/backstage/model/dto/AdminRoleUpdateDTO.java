package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 运营角色更新请求（/api/admin/role/update）。
 * <p>仅允许改 role_name / remark；role_code 不可改（permCode 字典稳定性）。
 */
@Data
public class AdminRoleUpdateDTO {
    /** 角色 ID（雪花）。 */
    private Long id;
    /** 角色名。 */
    private String roleName;
    /** 备注。 */
    private String remark;
}
