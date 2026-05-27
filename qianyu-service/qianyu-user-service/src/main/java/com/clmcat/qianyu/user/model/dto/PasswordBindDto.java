package com.clmcat.qianyu.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "当前登录用户绑定通用密码参数")
public class PasswordBindDto {
    /**
     * 要绑定的新密码
     */
    @Schema(description = "要绑定的新密码，长度 6-64")
    private String password;
}
