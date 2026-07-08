package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminLoginDTO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminAccountInfoVO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminLoginVO;
import com.clmcat.qianyu.mall.backstage.service.AdminAccountViewServiceBiz;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 运营账号 Controller：登录/登出/账号信息。
 * <p>类级 @LoginVerify(loginVerify=BackstageLoginVerifyFunction.class, token="X-Admin-Token")；
 * login 方法 @NoLoginVerify 放行（颁 token 前无需登录）。
 */
@Tag(name = "运营账号", description = "登录/登出/账号信息")
@ApiController
@RequestMapping("/api/admin")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class BackstageAccountController {

    @Resource
    private AdminAccountViewServiceBiz accountService;

    @Operation(summary = "登录（颁 adminToken）")
    @NoLoginVerify
    @PostMapping("/login")
    public AdminLoginVO login(@Params AdminLoginDTO dto, HttpServletRequest request) {
        return accountService.login(dto, getClientIp(request), request.getHeader("User-Agent"));
    }

    @Operation(summary = "登出（吊销 session）")
    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        accountService.logout(request.getHeader("X-Admin-Token"));
    }

    @Operation(summary = "当前账号信息（permCodes，前端动态路由硬前置）")
    @GetMapping("/account/info")
    public AdminAccountInfoVO accountInfo(@Token Long adminId) {
        return accountService.getAccountInfo(adminId);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
