package com.clmcat.qianyu.user.api.model.dto;

import lombok.Data;

@Data
public class EMailLoginDto {
    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;
    /**
     * 验证模式 手机号支持 邮件 code, password + 图形验证 code
     */
    private AuthMode authMode;
}
