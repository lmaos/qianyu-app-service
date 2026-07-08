package com.clmcat.qianyu.mall.backstage.support;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * backstage WebMvc 配置：注册 RequiresPermissionInterceptor（仅拦 /api/admin/**）。
 * <p>注意：@LoginVerify（BackstageLoginVerifyFunction）由框架 RequestInterceptor 处理（loginVerify + 鉴权）；
 * 本拦截器在其后（细粒度 permCode 校验）。
 */
@Configuration
public class BackstageWebMvcConfig implements WebMvcConfigurer {

    @Resource
    private RequiresPermissionInterceptor requiresPermissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requiresPermissionInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}
