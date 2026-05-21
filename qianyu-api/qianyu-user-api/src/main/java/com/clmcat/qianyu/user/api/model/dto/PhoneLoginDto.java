package com.clmcat.qianyu.user.api.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PhoneLoginDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 国家代码 "+86"
     */
    private String countryCode;
    /**
     * 短信验证码
     */
    private String code;
    /**
     * 验证模式 手机号支持 短信 code, password + 图形验证 code
     */
    private AuthMode authMode;
}
