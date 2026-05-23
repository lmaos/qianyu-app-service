package com.clmcat.qianyu.core.rpc;

import com.clmcat.qianyu.core.api.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcResponseSupport {


    public static <T> RpcResponse<T> build(com.clmcat.framework.webmvc.ResponseErrorStatus status, T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(status.getStatus());
        response.setState(status.getState());
        response.setMsg(status.getMessage());
        response.setData(data);
        return response;
    }

    public static <T> RpcResponse<T> FAIL(com.clmcat.framework.webmvc.error.ApiException status) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(status.getStatus());
        response.setState(status.getState());
        response.setMsg(status.getMessage());
        return response;
    }

    public static <T> RpcResponse<T> OK(T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(0);
        response.setState("OK");
        response.setMsg("OK");
        response.setData(data);
        return response;
    }

    public static <T> RpcResponse<T> FAIL(String msg) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(-1);
        response.setState("fail");
        response.setMsg(msg);
        return response;
    }

    private static final Map<String, Boolean> EXIST_CLASS_MAP = new ConcurrentHashMap<>();
    /**
     * 安全判断类是否存在
     */
    public static boolean exist(String className) {
        Boolean exist = EXIST_CLASS_MAP.get(className);
        if (exist == null) {
            try {
                Class.forName(className, false, RpcResponseSupport.class.getClassLoader());
                exist = true;
            } catch (ClassNotFoundException e) {
                exist = false;
            }
            EXIST_CLASS_MAP.put(className, exist);
        }
        return exist;
    }
}
