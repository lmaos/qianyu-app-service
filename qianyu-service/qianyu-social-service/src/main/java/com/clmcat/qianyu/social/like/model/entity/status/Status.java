package com.clmcat.qianyu.social.like.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus;

import java.io.Serializable;

public enum Status implements ResponseErrorStatus, Serializable {
    OK(ResponseStatus.OK.getStatus(), "OK", "一个成功的请求"),
    USER_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "用户ID必填", "用户ID必填"),
    MOMENT_ID_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "作品ID必填", "作品ID必填"),
    COMMENT_ID_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "评论ID必填", "评论ID必填"),
    MOMENT_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "作品不存在", "作品不存在"),
    COMMENT_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "评论不存在", "评论不存在"),
    COMMENT_NOT_LIKEABLE(ResponseStatus.R_OPERATION_FAIL.getStatus(), "当前评论不可点赞", "当前评论不可点赞");

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
