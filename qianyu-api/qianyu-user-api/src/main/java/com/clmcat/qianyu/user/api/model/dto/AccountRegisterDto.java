package com.clmcat.qianyu.user.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 使用账户密码注册
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AccountRegisterDto extends LoginDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 账户名
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
     * 客户端IP
     */
    private String clientIp;
}
