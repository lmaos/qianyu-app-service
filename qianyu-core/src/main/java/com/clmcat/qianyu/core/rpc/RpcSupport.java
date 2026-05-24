package com.clmcat.qianyu.core.rpc;

import com.clmcat.framework.webmvc.ResponseEntity;
import com.clmcat.qianyu.core.api.Rpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcSupport {


    public static <T> Rpc<T> build(com.clmcat.framework.webmvc.ResponseErrorStatus status, T data) {
        Rpc<T> response = new Rpc<>();
        response.setHttpStatus(status.getHttpStatus());
        response.setCode(status.getStatus());
        response.setState(status.getState());
        response.setMsg(status.getMessage());
        response.setData(data);
        return response;
    }


    public static <T> Rpc<T> build(int httpStatus, int status, String state, String msg, T data,  Class<?> rpcResponseType) {
        Rpc<T> response;
        try {
            response = (Rpc<T>) rpcResponseType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("创建RPC响应对象失败: {}", rpcResponseType.getName(), e);
            response = new Rpc<>();
        }
        response.setHttpStatus(httpStatus);
        response.setCode(status);
        response.setState(state);
        response.setMsg(msg);
        return response;
    }



    public static <T> Rpc<T> FAIL(com.clmcat.framework.webmvc.error.ApiException status) {
       return FAIL(status, Rpc.class);
    }

    public static <T> Rpc<T> FAIL(com.clmcat.framework.webmvc.error.ApiException apiEx,  Class<?> rpcResponseType) {
        ResponseEntity status = apiEx.create().build();
        return build(status.getHttpStatus(), status.getStatus(), status.getState(), status.getMessage(), null, rpcResponseType);
    }

    public static <T> Rpc<T> FAIL(org.apache.dubbo.rpc.RpcException status,  Class<?> rpcResponseType) {
        return build(500, status.getCode(), "RPCException", status.getMessage(), null, rpcResponseType);
    }

    public static <T> Rpc<T> OK(T data) {
        Rpc<T> response = new Rpc<>();
        response.setCode(0);
        response.setState("OK");
        response.setMsg("OK");
        response.setData(data);
        return response;
    }

    public static <T> Rpc<T> FAIL(String msg) {
        Rpc<T> response = new Rpc<>();
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
                Class.forName(className, false, RpcSupport.class.getClassLoader());
                exist = true;
            } catch (ClassNotFoundException e) {
                exist = false;
            }
            EXIST_CLASS_MAP.put(className, exist);
        }
        return exist;
    }

    public static boolean isRpcResponse(Invocation invocation) {
        return getRpcResponseType(invocation) != null;
    }

    public static Class<?> getRpcResponseType(Invocation invocation) {
        Invoker<?> invoker = invocation.getInvoker();
        Class<?> anInterface = invoker.getInterface();
        try {
            Method declaredMethod = anInterface.getDeclaredMethod(invocation.getMethodName(), invocation.getParameterTypes());
            if (Rpc.class.isAssignableFrom(declaredMethod.getReturnType())) {
                return declaredMethod.getReturnType();
            } else {
                return null;
            }
        }  catch (NoSuchMethodException e) {
            log.warn("方法未找到: {}.{}({})", anInterface.getName(), invocation.getMethodName(), invocation.getParameterTypes());
            return null;
        }
    }
}
