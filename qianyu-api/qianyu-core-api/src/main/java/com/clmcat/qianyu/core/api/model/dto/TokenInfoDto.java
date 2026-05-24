package com.clmcat.qianyu.core.api.model.dto;

import lombok.Data;

@Data
public class TokenInfoDto {
    private Long userId;
    private String country;
    private Long iat; // token创建的时间
    private Long exp; // 到期的时间
}
