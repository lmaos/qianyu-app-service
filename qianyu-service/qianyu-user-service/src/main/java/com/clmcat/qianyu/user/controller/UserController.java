package com.clmcat.qianyu.user.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Token;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户基础接口。
 * <p>
 * 当前类使用 {@link ApiController} 暴露 HTTP API，统一走 clmcat-webmvc 的响应包装链路。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "用户基础接口", description = "用户基础登录态接口，示例中包含 token 自动解析后的用户ID读取。")
@ApiController
@RequestMapping("/api/user")
@LoginVerify
public class UserController {

    /**
     * 读取当前登录用户ID。
     *
     * @param userId 当前登录用户ID，由 Token 自动解析注入
     * @return user:{userId} 文本
     */
    @Operation(
            summary = "读取当前登录用户ID",
            description = "用于验证 @Token 注入是否生效。参数说明：userId 由登录 token 自动解析，不需要前端手工传参。"
    )
    @GetMapping("/value")
    public String value(@Parameter(hidden = true) @Token long userId) {
        return "user:" + userId;
    }


}
