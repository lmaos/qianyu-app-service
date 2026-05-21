package com.clmcat.qianyu.user.api.model.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * 社交第三方登录
 */
public class SocialLoginDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private AuthPlatform platform;
    private String code;
}
