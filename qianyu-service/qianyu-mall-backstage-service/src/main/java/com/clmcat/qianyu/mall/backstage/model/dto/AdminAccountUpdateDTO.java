package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 运营账号更新请求（/api/admin/account/update、/resetPwd 复用）。
 * <p>仅允许改 real_name / mobile / email；username 不可改。
 * resetPwd 场景额外带 password（明文，Biz 层 BCrypt 哈希）。
 */
@Data
public class AdminAccountUpdateDTO {
    /** 账号 ID（雪花）。 */
    private Long id;
    /** 真实姓名。 */
    private String realName;
    /** 手机号。 */
    private String mobile;
    /** 邮箱。 */
    private String email;
    /** 重置密码场景：明文新密码（仅 resetPwd 使用；update 场景忽略）。 */
    private String password;
}
