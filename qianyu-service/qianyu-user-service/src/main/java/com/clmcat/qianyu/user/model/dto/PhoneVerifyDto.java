package com.clmcat.qianyu.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "手机号验证码发送参数")
public class PhoneVerifyDto {

    @Schema(description = "手机号，要求带国家码，例如 +86-13800000000")
    private String phone;

    @Schema(description = "预留的二次校验码，当前不参与校验", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String secondVerifyCode;
}
