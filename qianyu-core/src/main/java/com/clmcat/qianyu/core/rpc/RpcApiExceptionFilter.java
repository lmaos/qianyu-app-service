package com.clmcat.qianyu.core.rpc;

import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.framework.webmvc.ResponseEntity;
import com.clmcat.qianyu.core.api.Rpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * ApiException 封装
 */
@Activate(group = CommonConstants.PROVIDER) // 确保比 ExceptionFilter 更早执行包装
@Slf4j
public class RpcApiExceptionFilter implements Filter, Filter.Listener {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 什么都不做，直接交给后续的 Invoker
        // 注意：不要在 invoke 里直接修改 Result，因为异步情况下 Result 可能未完成
        return invoker.invoke(invocation);
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        // 此时 Result 已经包含最终结果（同步或异步完成）
        if (appResponse.hasException()) {
            handleException(appResponse, invoker, invocation);
        }
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        log.error("{}.{}", invoker.getInterface(), invocation.getMethodName(), t);
    }

    private void handleException(Result appResponse,  Invoker<?> invoker, Invocation invocation) {
        Throwable ex = appResponse.getException();
        // 如果检测到自定义 ApiException 且类存在
        if (RpcSupport.exist("com.clmcat.framework.webmvc.error.ApiException")
                && ex instanceof com.clmcat.framework.webmvc.error.ApiException apiEx) {

            ResponseEntity build = apiEx.create().build();
            Integer httpStatus = build.getHttpStatus();
            Integer status = build.getStatus();
            String state = build.getState();
            String message = build.getMessage();
            String localeMessage = build.getLocaleMessage();
            String errplace =  build.getErrplace();

            appResponse.setAttachment("apiEx.httpStatus", String.valueOf(httpStatus));
            appResponse.setAttachment("apiEx.status", String.valueOf(status));
            appResponse.setAttachment("apiEx.state", state);
            appResponse.setAttachment("apiEx.msg", message);
            appResponse.setAttachment("apiEx.name", "api");

            if (StringUtils.isNotBlank(localeMessage)) {
                appResponse.setAttachment("apiEx.localeMessage", localeMessage);
            }
            if (StringUtils.isNotBlank(errplace)) {
                appResponse.setAttachment("apiEx.errplace", errplace);
            }

            // return Rpc.OK(DATA);  如果应答结构是 Rpc 包装的。 将错误状态封装到应答中。 不带异常出去了。
            Class<?> rpcResponseType = RpcSupport.getRpcResponseType(invocation);
            if (rpcResponseType != null) {
                Rpc<?> fail = RpcSupport.FAIL(apiEx, rpcResponseType);
                appResponse.setValue(fail);
                appResponse.setException(null);
                if (log.isDebugEnabled()) {
                    log.debug("API Exception 转结果封装。 {}.{}; {}", invoker.getInterface(), invocation.getMethodName(), fail);
                }
            }

        } else if (ex instanceof RpcException rpcEx) {
            // 兼容 RPC Exception 透传
            appResponse.setAttachment("apiEx.name", "rpc");
            appResponse.setAttachment("apiEx.msg", rpcEx.getMessage());
            appResponse.setAttachment("apiEx.status", String.valueOf(rpcEx.getCode()));
            appResponse.setAttachment("apiEx.state", "RPCException");
            appResponse.setAttachment("apiEx.httpStatus", "500");

            // 如果应答结构是 Rpc 包装的。 将错误状态封装到应答中。
            Class<?> rpcResponseType = RpcSupport.getRpcResponseType(invocation);
            if (rpcResponseType != null) {
                Rpc<?> fail = RpcSupport.FAIL(rpcEx, rpcResponseType);
                appResponse.setValue(fail);
                appResponse.setException(null);
                if (log.isDebugEnabled()) {
                    log.debug("RPC Exception 转结果封装。 {}.{}; {}", invoker.getInterface(), invocation.getMethodName(), fail);
                }
            }
        }

    }


}