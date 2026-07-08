package com.clmcat.qianyu.mall.backstage.support;

import com.clmcat.framework.webmvc.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * 权限拦截器：读 request attribute ATTR_PERM_CODES（BackstageLoginVerifyFunction 注入），
 * 与 @RequiresPermission 声明的 permCode 求交集，无交集 AUTH_NO_PERMISSION。
 * <p>仅拦 /api/admin/**（BackstageWebMvcConfig 注册）。
 */
@Component
public class RequiresPermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) return true;
        HandlerMethod hm = (HandlerMethod) handler;
        RequiresPermission anno = hm.getMethodAnnotation(RequiresPermission.class);
        if (anno == null) anno = hm.getBeanType().getAnnotation(RequiresPermission.class);
        if (anno == null) return true; // 无注解放行（仅登录校验，无细粒度权限）

        @SuppressWarnings("unchecked")
        List<String> permCodes = (List<String>) request.getAttribute(BackstageLoginVerifyFunction.ATTR_PERM_CODES);
        if (permCodes == null || permCodes.isEmpty()) {
            ResponseStatus.AUTH_NO_PERMISSION.assertThrowResEx("无权限访问", true);
        }
        for (String required : anno.value()) {
            if (permCodes.contains(required)) return true; // 满足任一即放行
        }
        ResponseStatus.AUTH_NO_PERMISSION.assertThrowResEx(
                "无权限：" + Arrays.toString(anno.value()), true);
        return false;
    }
}
