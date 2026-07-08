package com.clmcat.qianyu.config;

import com.clmcat.framework.webmvc.interceptor.CorsConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * 应用级 CORS 配置
 * <p>
 * 覆盖框架默认的 {@link CorsConfig.DefaultCorsConfig}，
 * 将 Access-Control-Allow-Origin 设为请求方 Origin（而非 *），
 * 解决浏览器在 credentials 模式下拒绝 * 通配符的问题。
 *
 * @author author
 * @date 2025-01-01
 */
@Component
public class AppCorsConfig implements CorsConfig {

    @Override
    public void setCors(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin == null || origin.isBlank()) {
            origin = "*";
        }

        String requestHeaders = request.getHeader("Access-Control-Request-Headers");
        if (requestHeaders == null || requestHeaders.isBlank()) {
            requestHeaders = "*";
        }

        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Headers", requestHeaders);
        response.setHeader("Access-Control-Expose-Headers", requestHeaders);
        response.setHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST, PUT, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
