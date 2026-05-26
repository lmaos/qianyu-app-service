package com.clmcat.qianyu.core.openapi;

import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Token;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 适配项目自定义控制器注解到 OpenAPI Operation。
 */
public class QianyuOpenApiOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        hideTokenParameters(operation, handlerMethod);
        adaptLoginVerify(operation, handlerMethod);
        addTokenHeaderParameter(operation, handlerMethod);
        appendTokenHeaderDescription(operation, handlerMethod);
        return operation;
    }

    private void adaptLoginVerify(Operation operation, HandlerMethod handlerMethod) {
        LoginVerify loginVerify = findLoginVerify(handlerMethod);
        if (loginVerify == null) {
            return;
        }

        Map<String, Object> extension = new LinkedHashMap<>();
        extension.put("mustLogin", loginVerify.mustLogin());
        extension.put("tokenHeader", loginVerify.token());
        extension.put("userIdField", loginVerify.userId());
        operation.addExtension("x-qianyu-login-verify", extension);

        if (loginVerify.mustLogin()) {
            operation.addSecurityItem(new SecurityRequirement().addList(OpenApiAutoConfiguration.SECURITY_SCHEME_NAME));
        }
    }

    private void hideTokenParameters(Operation operation, HandlerMethod handlerMethod) {
        if (CollectionUtils.isEmpty(operation.getParameters())) {
            return;
        }

        Set<String> hiddenParameterNames = new LinkedHashSet<>();
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            Token token = methodParameter.getParameterAnnotation(Token.class);
            if (token == null) {
                continue;
            }
            if (StringUtils.hasText(methodParameter.getParameterName())) {
                hiddenParameterNames.add(methodParameter.getParameterName());
            }
            if (StringUtils.hasText(token.value())) {
                hiddenParameterNames.add(token.value());
            }
            if (StringUtils.hasText(token.userId())) {
                hiddenParameterNames.add(token.userId());
            }
        }
        if (hiddenParameterNames.isEmpty()) {
            return;
        }
        operation.getParameters().removeIf(parameter -> hiddenParameterNames.contains(parameter.getName()));
    }

    private void appendTokenHeaderDescription(Operation operation, HandlerMethod handlerMethod) {
        LoginVerify loginVerify = findLoginVerify(handlerMethod);
        boolean hasTokenParameter = hasTokenParameter(handlerMethod);
        if (loginVerify == null && !hasTokenParameter) {
            return;
        }
        String tokenHeader = loginVerify != null && StringUtils.hasText(loginVerify.token()) ? loginVerify.token() : "token";
        String authDescription = "鉴权说明：当前接口使用 @LoginVerify 或 @Token，请在请求头携带 " + tokenHeader + "；Swagger UI 调试时可点击右上角 Authorize 后统一填写。";
        if (!StringUtils.hasText(operation.getDescription())) {
            operation.setDescription(authDescription);
            return;
        }
        if (!operation.getDescription().contains(authDescription)) {
            operation.setDescription(operation.getDescription() + "<br/><br/>" + authDescription);
        }
    }

    private void addTokenHeaderParameter(Operation operation, HandlerMethod handlerMethod) {
        LoginVerify loginVerify = findLoginVerify(handlerMethod);
        boolean hasTokenParameter = hasTokenParameter(handlerMethod);
        if (loginVerify == null && !hasTokenParameter) {
            return;
        }
        String tokenHeader = loginVerify != null && StringUtils.hasText(loginVerify.token()) ? loginVerify.token() : "token";
        boolean required = hasTokenParameter || (loginVerify != null && loginVerify.mustLogin());
        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                if ("header".equals(parameter.getIn()) && tokenHeader.equals(parameter.getName())) {
                    parameter.setRequired(required);
                    if (!StringUtils.hasText(parameter.getDescription())) {
                        parameter.setDescription("登录 token，请放在请求头 " + tokenHeader + " 中。");
                    }
                    return;
                }
            }
        }
        operation.addParametersItem(new Parameter()
                .in("header")
                .name(tokenHeader)
                .required(required)
                .description("登录 token，请放在请求头 " + tokenHeader + " 中。")
                .schema(new StringSchema()));
    }

    private boolean hasTokenParameter(HandlerMethod handlerMethod) {
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            if (methodParameter.hasParameterAnnotation(Token.class)) {
                return true;
            }
        }
        return false;
    }

    private LoginVerify findLoginVerify(HandlerMethod handlerMethod) {
        LoginVerify methodLoginVerify = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), LoginVerify.class);
        if (methodLoginVerify != null) {
            return methodLoginVerify;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), LoginVerify.class);
    }
}
