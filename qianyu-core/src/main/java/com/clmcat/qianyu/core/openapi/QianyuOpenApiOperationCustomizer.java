package com.clmcat.qianyu.core.openapi;

import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Token;
import io.swagger.v3.oas.models.Operation;
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

    private LoginVerify findLoginVerify(HandlerMethod handlerMethod) {
        LoginVerify methodLoginVerify = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), LoginVerify.class);
        if (methodLoginVerify != null) {
            return methodLoginVerify;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), LoginVerify.class);
    }
}
