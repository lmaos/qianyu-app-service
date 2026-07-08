package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 运营账号分页查询请求（/api/admin/account/page）。
 * <p>keyword 模糊匹配 username / real_name / mobile；status null=不限 1启用/0禁用/2冻结。
 */
@Data
public class AdminAccountPageQueryDTO {
    /** 页码，从 1 开始；null/<=0 兜底为 1。 */
    private Integer pageNum;
    /** 每页大小；null/<=0 兜底为 10。 */
    private Integer pageSize;
    /** 关键字（username / real_name / mobile 模糊匹配，OR）。 */
    private String keyword;
    /** 状态过滤：null=不限，1=启用，0=禁用，2=冻结。 */
    private Integer status;
}
