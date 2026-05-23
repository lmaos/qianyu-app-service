package com.clmcat.qianyu.core.rpc;

import com.clmcat.qianyu.core.api.RpcResponse;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Activate(group = CommonConstants.CONSUMER, order = 9999)
public class RpcUnpackFilter implements BaseFilter, BaseFilter.Listener {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 只负责传递，不处理结果
        return invoker.invoke(invocation);
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        // 此时结果已经就绪（同步或异步完成）
        if (appResponse.hasException()) {
            return; // 异常情况直接透传，不处理
        }

        Object value = appResponse.getValue();
        if (value instanceof RpcResponse<?> resp) {
            if (resp.getCode() == 0) {
                // 成功：解包 data
                appResponse.setValue(resp.getData());
            } else {
                // 失败：转换为异常
                if (RpcResponseSupport.exist("com.clmcat.framework.webmvc.error.ApiException")) {
                    com.clmcat.framework.webmvc.ResponseStatus.ErrorStatus status =
                            com.clmcat.framework.webmvc.ResponseStatus.ErrorStatus.of(resp.getCode(), resp.getState());
                    appResponse.setException(
                            new com.clmcat.framework.webmvc.error.ApiResultException(status, resp.getMsg())
                    );
                } else {
                    appResponse.setException(new RpcException(resp.getCode(), resp.getMsg()));
                }
                appResponse.setValue(null); // 清除原值
            }
        }
        // 其他情况保持不变
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        // 框架异常直接透传，可以记录日志
        // 不修改结果，因为已经是错误状态
    }
}