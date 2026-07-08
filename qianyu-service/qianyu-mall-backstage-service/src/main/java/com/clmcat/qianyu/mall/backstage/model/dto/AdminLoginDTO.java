package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/** 运营后台登录请求。 */
@Data
public class AdminLoginDTO {
    private String username;
    private String password;
}
