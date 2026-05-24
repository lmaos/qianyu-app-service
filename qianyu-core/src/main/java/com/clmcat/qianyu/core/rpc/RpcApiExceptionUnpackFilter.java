package com.clmcat.qianyu.core.rpc;

import com.clmcat.qianyu.core.api.Rpc;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * ApiException 解包
 */
@Activate(group = CommonConstants.CONSUMER)
public class RpcApiExceptionUnpackFilter implements Filter, BaseFilter.Listener {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 只负责传递，不处理结果，因为结果可能是异步的，必须等到 onResponse 时才能处理
        return invoker.invoke(invocation);
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        // 1. 优先处理通过附件传递的 ApiException
        String apiExName = appResponse.getAttachment("apiEx.name");
        if (StringUtils.isNotBlank(apiExName)) {
            // 读取附件数据
            String state = appResponse.getAttachment("apiEx.state");
            int httpStatus = NumberUtils.toInt(appResponse.getAttachment("apiEx.httpStatus"));
            int status = NumberUtils.toInt(appResponse.getAttachment("apiEx.status"));
            String message = appResponse.getAttachment("apiEx.message");
            String localeMessage = appResponse.getAttachment("apiEx.localeMessage");
            String errplace = appResponse.getAttachment("apiEx.errplace");

            // 如果 ApiException 类存在，包装为 ApiException
            if (RpcSupport.exist("com.clmcat.framework.webmvc.error.ApiException")) {
                com.clmcat.framework.webmvc.ResponseErrorStatus errorStatus = buildErrorStatus(
                        httpStatus, state, status, message, localeMessage);
                com.clmcat.framework.webmvc.error.ApiException apiEx =
                        new com.clmcat.framework.webmvc.error.ApiException(errorStatus, message, errplace);
                appResponse.setException(apiEx);
            } else if (appResponse.hasException() && appResponse.getException() instanceof RpcException) {
                // RpcException 正常忽略，不做处理
            } else {
                // 其他情况创建普通 RpcException
                appResponse.setException(new RpcException(status, message));
            }
            return; // 附件处理完毕，不再执行后续逻辑
        }

        // 2. 处理 Rpc 返回值失败的情况
        Class<?> rpcResponseType = RpcSupport.getRpcResponseType(invocation);
        if (rpcResponseType != null && RpcSupport.exist("com.clmcat.framework.webmvc.error.ApiException")) {
            Object value = appResponse.getValue();
            if (value instanceof Rpc && !((Rpc<?>) value).success()) {
                Rpc<?> rpc = (Rpc<?>) value;
                com.clmcat.framework.webmvc.ResponseErrorStatus errorStatus = buildErrorStatusFromRpc(rpc);
                appResponse.setException(new com.clmcat.framework.webmvc.error.ApiResultException(errorStatus));
            }
        }
        // 其他情况（包括成功、或未匹配到任何规则）不做任何处理
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        // 框架异常直接透传，可以记录日志
        // 不修改结果，因为已经是错误状态
    }

    // ========== 辅助方法：消除重复的匿名内部类 ==========

    private com.clmcat.framework.webmvc.ResponseErrorStatus buildErrorStatus(
            int httpStatus, String state, Integer status, String message, String localeMessage) {
        return new com.clmcat.framework.webmvc.ResponseErrorStatus() {
            @Override
            public int getHttpStatus() { return httpStatus; }
            @Override
            public String getState() { return state; }
            @Override
            public Integer getStatus() { return status; }
            @Override
            public String getMessage() { return message; }
            @Override
            public String getLocaleMessage() { return StringUtils.isBlank(localeMessage) ? state : localeMessage; }
        };
    }

    private com.clmcat.framework.webmvc.ResponseErrorStatus buildErrorStatusFromRpc(Rpc<?> rpc) {
        return new com.clmcat.framework.webmvc.ResponseErrorStatus() {
            @Override
            public int getHttpStatus() { return rpc.getHttpStatus(); }
            @Override
            public String getState() { return rpc.getState(); }
            @Override
            public Integer getStatus() { return rpc.getCode(); }
            @Override
            public String getMessage() { return rpc.getMsg(); }
            @Override
            public String getLocaleMessage() { return rpc.getMsg(); } // Rpc 无 localeMessage，降级
        };
    }
}