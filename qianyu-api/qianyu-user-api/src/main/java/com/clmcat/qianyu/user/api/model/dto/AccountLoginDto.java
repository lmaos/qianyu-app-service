package com.clmcat.qianyu.user.api.model.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 使用账户登录
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AccountLoginDto extends LoginDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 手机号
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 图形验证码
     */
    private String code;

    /**
     * 用户ID
     */
    private String clientIp;

}
