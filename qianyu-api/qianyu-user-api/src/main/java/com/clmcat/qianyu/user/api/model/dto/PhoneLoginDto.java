package com.clmcat.qianyu.user.api.model.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PhoneLoginDto extends LoginDto implements Serializable {
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

    /**
     * 用户ID
     */
    private String clientIp;
}
