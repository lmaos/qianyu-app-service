package com.clmcat.qianyu.social.visitor.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus;

import java.io.Serializable;

public enum Status implements ResponseErrorStatus, Serializable {
    OK(ResponseStatus.OK.getStatus(), "OK", "一个成功的请求"),
    USER_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "用户ID必填", "用户ID必填"),
    ;

    Status(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    Status(Integer status, String message, String describe) {
        this.status = status;
        this.message = message;
        this.describe = describe;
    }

    private final int httpStatus = 200;
    private Integer status;
    private String message;
    private String describe;

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getState() {
        return name();
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
