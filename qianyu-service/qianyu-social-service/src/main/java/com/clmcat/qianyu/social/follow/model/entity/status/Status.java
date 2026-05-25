package com.clmcat.qianyu.social.follow.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus;

import java.io.Serializable;

public enum Status implements ResponseErrorStatus, Serializable {
    OK(ResponseStatus.OK.getStatus(), "OK", "一个成功的请求"),
    FOLLOWER_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "关注者ID必填", "关注者ID必填"),
    FOLLOWEE_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "被关注用户ID必填", "被关注用户ID必填"),
    QUERY_USER_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "查询用户ID必填", "查询用户ID必填"),
    FOLLOW_SELF_NOT_ALLOWED(ResponseStatus.P_VALUE_ERROR.getStatus(), "不能关注自己", "不能关注自己");

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
