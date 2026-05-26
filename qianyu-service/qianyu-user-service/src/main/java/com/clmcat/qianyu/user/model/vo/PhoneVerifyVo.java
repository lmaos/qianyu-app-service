package com.clmcat.qianyu.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "手机号验证码发送结果")
public class PhoneVerifyVo {
    // 需要二次验证码
    @Schema(description = "是否需要进行二次校验")
    private boolean needSecondVerify;

}
