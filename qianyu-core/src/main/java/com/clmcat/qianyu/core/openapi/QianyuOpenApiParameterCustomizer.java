package com.clmcat.qianyu.core.openapi;

import com.clmcat.framework.webmvc.anns.Params;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ValueConstants;

/**
 * 适配项目自定义 {@link Params} 注解到 OpenAPI 参数元数据。
 */
public class QianyuOpenApiParameterCustomizer implements ParameterCustomizer {

    @Override
    public Parameter customize(Parameter parameterModel, MethodParameter methodParameter) {
        Params params = methodParameter.getParameterAnnotation(Params.class);
        if (params == null || parameterModel == null) {
            return parameterModel;
        }

        if (StringUtils.hasText(params.name())) {
            parameterModel.setName(params.name());
        }
        parameterModel.setRequired(params.required());
        parameterModel.setIn(mapScope(params.scope()));
        if (!ValueConstants.DEFAULT_NONE.equals(params.defaultValue())) {
            parameterModel.setExample(params.defaultValue());
        }
        return parameterModel;
    }

    private String mapScope(Params.ParamsScope scope) {
        if (scope == null) {
            return "query";
        }
        return switch (scope) {
            case HEADER -> "header";
            case COOKIE -> "cookie";
            default -> "query";
        };
    }
}
