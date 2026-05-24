package com.clmcat.qianyu.user.model.vo;

import lombok.Data;

@Data
public class PhoneVerifyVo {
    // 需要二次验证码
    private boolean needSecondVerify;

}
