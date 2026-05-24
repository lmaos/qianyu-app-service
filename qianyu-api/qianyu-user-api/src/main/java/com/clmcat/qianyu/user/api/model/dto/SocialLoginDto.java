package com.clmcat.qianyu.user.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 社交第三方登录
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginDto extends LoginDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private AuthPlatform platform;

    private String code;

    /**
     * 用户ID
     */
    private String clientIp;
}
