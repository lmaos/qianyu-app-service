package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 给运营账号分配角色请求（/api/admin/account/assignRoles）。
 * <p>Biz 层先 deleteByQuery(account_id) 清旧关联，再批量 insertSelective 新关联（先删后插，幂等）。
 */
@Data
public class AdminAccountAssignRolesDTO {
    /** 账号 ID（雪花）。 */
    private Long accountId;
    /** 角色 ID 列表（全量覆盖；空列表=清空角色）。 */
    private List<Long> roleIds;
}
