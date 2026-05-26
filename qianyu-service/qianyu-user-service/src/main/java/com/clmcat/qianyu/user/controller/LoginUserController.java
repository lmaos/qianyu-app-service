package com.clmcat.qianyu.user.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.user.api.model.dto.AccountLoginDto;
import com.clmcat.qianyu.user.api.model.dto.EMailLoginDto;
import com.clmcat.qianyu.user.api.model.dto.PhoneLoginDto;
import com.clmcat.qianyu.user.api.model.dto.SocialLoginDto;
import com.clmcat.qianyu.user.api.model.dto.SignerDto;
import com.clmcat.qianyu.user.model.vo.LoginResultVo;
import com.clmcat.qianyu.user.service.UserLoginServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户登录相关接口。
 * <p>
 * 当前类使用 {@link ApiController} 暴露 HTTP API，包含手机号、邮箱、社交登录、账号登录和签名验证能力。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "用户登录接口", description = "提供手机号、邮箱、社交、账号登录，以及 token 签名/验签接口。")
@ApiController
@RequestMapping("/api/user/login")
@Slf4j
public class LoginUserController {

    @Resource
    UserLoginServiceBiz userLoginServiceBiz;


    /**
     * 手机号登录。
     *
     * @param dto 登录参数，包含 phone、countryCode、code、authMode、clientIp
     * @return 登录结果
     */
    @Operation(summary = "手机号登录", description = "参数说明：phone 为手机号，countryCode 为国家区号，code 为短信验证码，authMode 为验证模式，clientIp 为客户端IP。")
    @PostMapping("/phone")
    public LoginResultVo phone(@RequestBody(description = "手机号登录参数") @Params PhoneLoginDto dto) {
        return userLoginServiceBiz.phone(dto);
    }

    /**
     * 邮箱登录。
     *
     * @param dto 登录参数，包含 email、code、authMode、clientIp
     * @return 登录结果
     */
    @Operation(summary = "邮箱登录", description = "参数说明：email 为邮箱地址，code 为邮箱验证码，authMode 为验证模式，clientIp 为客户端IP。")
    @PostMapping("/email")
    public LoginResultVo email(@RequestBody(description = "邮箱登录参数") @Params EMailLoginDto dto) {
        return userLoginServiceBiz.email(dto);
    }

    /**
     * 第三方社交登录。
     *
     * @param dto 登录参数，包含 platform、code、clientIp
     * @return 登录结果
     */
    @Operation(summary = "社交登录", description = "参数说明：platform 为第三方平台类型，code 为平台返回的授权码，clientIp 为客户端IP。")
    @PostMapping("/social")
    public LoginResultVo social(@RequestBody(description = "第三方社交登录参数") @Params SocialLoginDto dto) {
        return userLoginServiceBiz.social(dto);
    }

    /**
     * 账号密码登录。
     *
     * @param dto 登录参数，包含 username、password、code、clientIp
     * @return 登录结果
     */
    @Operation(summary = "账号密码登录", description = "参数说明：username 为登录账号，password 为密码，code 为图形验证码，clientIp 为客户端IP。")
    @PostMapping("/account")
    public LoginResultVo account(@RequestBody(description = "账号密码登录参数") @Params AccountLoginDto dto) {
        log.info("account:{}", dto);
        return userLoginServiceBiz.account(dto);
    }


    /**
     * 生成登录签名 token。
     *
     * @param dto 签名参数，继承 TokenInfoDto 中定义的用户信息字段
     * @return token 字符串
     */
    @Operation(summary = "生成登录签名", description = "参数说明：SignerDto 继承 TokenInfoDto，请传入需要写入 token 的用户身份信息。")
    @PostMapping("/signer")
    public String signer(@RequestBody(description = "签名参数，继承 TokenInfoDto") @Params SignerDto dto) {
        return userLoginServiceBiz.signer(dto);
    }

    /**
     * 解析并验证 token。
     *
     * @param token 待解析验证的 token
     * @return token 中的签名信息
     */
    @Operation(summary = "解析并验证 token", description = "参数说明：token 为待验证的登录 token 字符串。")
    @PostMapping("/verifier")
    public SignerDto verifier(@Parameter(description = "待解析验证的 token 字符串", required = true) @Params("token") String token) {
        return userLoginServiceBiz.verifier(token);
    }

}
