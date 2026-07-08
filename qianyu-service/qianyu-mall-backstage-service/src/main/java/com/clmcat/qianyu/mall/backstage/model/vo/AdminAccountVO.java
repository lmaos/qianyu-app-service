package com.clmcat.qianyu.mall.backstage.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 运营账号列表项 VO（/api/admin/account/page）。
 * <p>roleNames 由 Biz 层富化：account → account_role → role 聚合 role_name。
 */
@Data
@Builder
public class AdminAccountVO {
    /** 账号 ID（雪花）。 */
    private Long id;
    /** 登录用户名。 */
    private String username;
    /** 真实姓名。 */
    private String realName;
    /** 手机号。 */
    private String mobile;
    /** 邮箱。 */
    private String email;
    /** 状态：1启用/0禁用/2冻结。 */
    private Integer status;
    /** 最近登录时间（毫秒戳）。 */
    private Long lastLoginAt;
    /** 创建时间（毫秒戳）。 */
    private Long createTime;
    /** 该账号拥有的角色名列表（Biz 层富化）。 */
    private List<String> roleNames;
}
