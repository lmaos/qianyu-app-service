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
public class EMailLoginDto extends LoginDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    /**
     * 用户ID
     */
    private String clientIp;
}
