package com.clmcat.qianyu.social.comment.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus;

import java.io.Serializable;

public enum Status implements ResponseErrorStatus, Serializable {
    OK(ResponseStatus.OK.getStatus(), "OK", "一个成功的请求"),
    COMMENT_ID_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "评论ID必填", "评论ID必填"),
    MOMENT_ID_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "作品ID必填", "作品ID必填"),
    AUTHOR_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "评论作者ID必填", "评论作者ID必填"),
    COMMENT_CONTENT_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "评论内容必填", "评论内容必填"),
    COMMENT_TEXT_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "评论文本必填", "评论文本必填"),
    COMMENT_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "评论不存在", "评论不存在"),
    PARENT_COMMENT_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "父评论不存在", "父评论不存在"),
    REPLY_COMMENT_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "回复目标评论不存在", "回复目标评论不存在"),
    COMMENT_REPLY_DENIED(ResponseStatus.R_OPERATION_FAIL.getStatus(), "当前评论不可回复", "当前评论不可回复"),
    COMMENT_DELETE_DENIED(ResponseStatus.A_ACCESS_DENIED.getStatus(), "无权删除该评论", "无权删除该评论"),
    COMMENT_SAVE_FAIL(ResponseStatus.R_OPERATION_FAIL.getStatus(), "评论发布失败", "评论发布失败"),
    COMMENT_DELETE_FAIL(ResponseStatus.R_OPERATION_FAIL.getStatus(), "评论删除失败", "评论删除失败");

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
