package com.clmcat.qianyu.user.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录成功返回的 TOKEN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 授权通过的 TOKEN
     */
    private String token;
    /**
     * 登录的用户ID
     */
    private long userId;
}
