package com.clmcat.qianyu.core.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 自动配置。
 * <p>
 * 仅当运行环境引入了 springdoc 依赖时才生效，用于让项目里的自定义
 * {@code @ApiController}、{@code @LoginVerify}、{@code @Token}、{@code @Params}
 * 能更自然地映射到 OpenAPI 文档。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = {
        "io.swagger.v3.oas.models.OpenAPI",
        "org.springdoc.core.models.GroupedOpenApi",
        "org.springdoc.core.customizers.OperationCustomizer",
        "org.springdoc.core.customizers.ParameterCustomizer"
})
public class OpenApiAutoConfiguration {

    public static final String SECURITY_SCHEME_NAME = "qianyuToken";

    /**
     * 提供全局 OpenAPI 基础信息和 token 安全定义。
     */
    @Bean
    @ConditionalOnMissingBean
    OpenAPI qianyuOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Qianyu HTTP API")
                        .version("v1")
                        .description("Qianyu 项目自动生成的 HTTP API 文档，适合前端联调与 AI 读取。"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("token")
                                .description("Qianyu 登录 token。凡是使用 @LoginVerify 或 @Token 的接口，调用时都需要在请求头携带 token；Swagger UI 调试时可点击右上角 Authorize 统一填写。")));
    }

    /**
     * 为业务 HTTP 接口提供单独分组，方便 `/v3/api-docs/http-api` 给前端或 AI 读取。
     */
    @Bean
    @ConditionalOnMissingBean(name = "qianyuHttpApiGroupedOpenApi")
    GroupedOpenApi qianyuHttpApiGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("http-api")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "qianyuOpenApiOperationCustomizer")
    OperationCustomizer qianyuOpenApiOperationCustomizer() {
        return new QianyuOpenApiOperationCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean(name = "qianyuOpenApiParameterCustomizer")
    ParameterCustomizer qianyuOpenApiParameterCustomizer() {
        return new QianyuOpenApiParameterCustomizer();
    }
}
