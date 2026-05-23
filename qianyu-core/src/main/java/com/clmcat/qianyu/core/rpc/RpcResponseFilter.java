package com.clmcat.qianyu.core.rpc;

import com.clmcat.qianyu.core.api.RpcResponse;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.model.BuiltinServiceDetector;

@Activate(group = CommonConstants.PROVIDER, order = -20000) // 确保比 ExceptionFilter 更早执行包装
public class RpcResponseFilter implements BaseFilter, BaseFilter.Listener {

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
            handleException(appResponse);
        } else {
            handleNormalResponse(appResponse);
        }
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {

    }

    private void handleException(Result appResponse) {
        Throwable ex = appResponse.getException();

        // 如果检测到自定义 ApiException 且类存在
        if (RpcResponseSupport.exist("com.clmcat.framework.webmvc.error.ApiException")
                && ex instanceof com.clmcat.framework.webmvc.error.ApiException apiEx) {
            RpcResponse<?> failResp = RpcResponseSupport.FAIL(apiEx);
            appResponse.setValue(failResp);
            appResponse.setException(null); // 清除原始异常，因为已经包装为正常响应
        } else {
            // 通用异常包装
            RpcResponse<?> failResp = RpcResponseSupport.FAIL(ex.getMessage());
            appResponse.setValue(failResp);
            appResponse.setException(null);
        }
    }

    private void handleNormalResponse(Result appResponse) {
        Object value = appResponse.getValue();
        if (value instanceof RpcResponse<?>) {
            return; // 已经是 RpcResponse，避免二次包装
        }
        RpcResponse<?> successResp = RpcResponseSupport.OK(value);
        appResponse.setValue(successResp);
    }
}