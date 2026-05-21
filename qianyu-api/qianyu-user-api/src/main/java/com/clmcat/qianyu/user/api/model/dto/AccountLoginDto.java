package com.clmcat.qianyu.user.api.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class AccountLoginDto  implements Serializable {
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

}
