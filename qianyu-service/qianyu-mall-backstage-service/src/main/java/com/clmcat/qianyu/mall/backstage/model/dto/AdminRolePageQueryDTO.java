package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 运营角色分页查询请求（/api/admin/role/page）。
 * <p>keyword 模糊匹配 role_code / role_name。
 */
@Data
public class AdminRolePageQueryDTO {
    /** 页码，从 1 开始；null/<=0 兜底为 1。 */
    private Integer pageNum;
    /** 每页大小；null/<=0 兜底为 10。 */
    private Integer pageSize;
    /** 关键字（role_code / role_name 模糊匹配，OR）。 */
    private String keyword;
}
