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
        // 此时结果已经就绪（同步或异步完成）
        String apiExName  = appResponse.getAttachment("apiEx.name");
        // 存在 ApiException 定义
        if (StringUtils.isNotBlank(apiExName)) {
            // 读取数据
            String state  = appResponse.getAttachment("apiEx.state");
            int httpStatus = NumberUtils.toInt(appResponse.getAttachment("apiEx.httpStatus"));
            int status = NumberUtils.toInt(appResponse.getAttachment("apiEx.status"));
            String message =  appResponse.getAttachment("apiEx.message");
            String localeMessage =  appResponse.getAttachment("apiEx.localeMessage");
            String errplace = appResponse.getAttachment("apiEx.errplace");
            // 验证存在 ApiException 定义，避免版本不一致导致的类不存在问题
            if (RpcSupport.exist("com.clmcat.framework.webmvc.error.ApiException")) {
                com.clmcat.framework.webmvc.ResponseErrorStatus errorStatus = new com.clmcat.framework.webmvc.ResponseErrorStatus() {
                    @Override
                    public int getHttpStatus() {
                        return httpStatus;
                    }

                    @Override
                    public String getState() {
                        return state;
                    }

                    @Override
                    public Integer getStatus() {
                        return status;
                    }

                    @Override
                    public String getMessage() {
                        return message;
                    }

                    @Override
                    public String getLocaleMessage() {
                        return StringUtils.isBlank(localeMessage) ? state : localeMessage;
                    }
                };
                com.clmcat.framework.webmvc.error.ApiException apiEx = new com.clmcat.framework.webmvc.error.ApiException(errorStatus, message, errplace);

                appResponse.setException(apiEx);
            } else {
                appResponse.setException(new RpcException(status, message));
            }
        } else {
            Class<?> rpcResponseType = RpcSupport.getRpcResponseType(invocation);
            // RPC 应答 并且 存在 com.clmcat.framework.webmvc.error.ApiException 时
            if (rpcResponseType != null && RpcSupport.exist("com.clmcat.framework.webmvc.error.ApiException")) {
                Rpc<?> value = (Rpc<?>)appResponse.getValue();
                // 未成功的时候。
                if (value != null && !value.success()) {
                    com.clmcat.framework.webmvc.ResponseErrorStatus errorStatus = new com.clmcat.framework.webmvc.ResponseErrorStatus() {
                        @Override
                        public int getHttpStatus() {
                            return value.getHttpStatus();
                        }

                        @Override
                        public String getState() {
                            return value.getState();
                        }

                        @Override
                        public Integer getStatus() {
                            return value.getCode();
                        }

                        @Override
                        public String getMessage() {
                            return value.getMsg();
                        }

                    };
                    appResponse.setException(new com.clmcat.framework.webmvc.error.ApiResultException(errorStatus));
                }

            }
        }
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        // 框架异常直接透传，可以记录日志
        // 不修改结果，因为已经是错误状态
    }
}