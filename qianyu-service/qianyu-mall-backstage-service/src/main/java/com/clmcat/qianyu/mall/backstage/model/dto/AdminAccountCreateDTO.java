package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 运营账号创建请求（/api/admin/account/create）。
 * <p>password 明文入参，Biz 层 BCrypt(cost=12) 哈希后落 pwd_hash。
 */
@Data
public class AdminAccountCreateDTO {
    /** 登录用户名（唯一）。 */
    private String username;
    /** 明文密码（Biz 层 BCrypt 哈希）。 */
    private String password;
    /** 真实姓名。 */
    private String realName;
    /** 手机号。 */
    private String mobile;
    /** 邮箱。 */
    private String email;
}
