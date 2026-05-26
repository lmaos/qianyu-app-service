package com.clmcat.qianyu.storage.model.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus;

import java.io.Serializable;

/**
 * 文件存储状态
 */
public enum Status implements ResponseErrorStatus, Serializable {

    OK(ResponseStatus.OK.getStatus(), "OK", "一个成功的请求"),
    /** 上传文件不能为空 */
    FILE_EMPTY(ResponseStatus.P_VALUE_ERROR.getStatus(), "上传文件不能为空", "上传文件不能为空"),
    /** 文件大小超出限制 */
    FILE_TOO_LARGE(ResponseStatus.P_VALUE_ERROR.getStatus(), "文件大小超出限制", "文件大小超出限制"),
    /** 不支持的文件类型 */
    FILE_TYPE_NOT_ALLOWED(ResponseStatus.P_VALUE_ERROR.getStatus(), "不支持的文件类型", "不支持的文件类型"),
    /** 文件key必填 */
    STORAGE_KEY_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "文件key必填", "文件key必填"),
    /** 文件上传失败 */
    STORAGE_UPLOAD_FAIL(ResponseStatus.R_OPERATION_FAIL.getStatus(), "文件上传失败", "文件上传失败"),
    /** 文件删除失败 */
    STORAGE_DELETE_FAIL(ResponseStatus.R_OPERATION_FAIL.getStatus(), "文件删除失败", "文件删除失败"),
    /** 预签名链接生成失败 */
    STORAGE_PRESIGN_FAIL(ResponseStatus.R_OPERATION_FAIL.getStatus(), "预签名链接生成失败", "预签名链接生成失败"),

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

    private int httpStatus = 200;
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
