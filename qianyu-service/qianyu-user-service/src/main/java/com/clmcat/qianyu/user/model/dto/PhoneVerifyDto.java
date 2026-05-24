package com.clmcat.qianyu.user.model.dto;

import lombok.Data;

@Data
public class PhoneVerifyDto {

    private String phone;

    private String secondVerifyCode;
}
