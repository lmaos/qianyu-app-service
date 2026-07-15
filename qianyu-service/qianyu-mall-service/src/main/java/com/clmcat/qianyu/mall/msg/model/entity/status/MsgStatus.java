package com.clmcat.qianyu.mall.msg.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

/**
 * 系统通知模块错误码（msg 域，411xxx）。
 * <p>实现 {@link ResponseErrorStatus}，配合 {@code assertThrowResEx} 抛 {@code ApiResultException}（与 MchStatus 等同范式）。
 */
public enum MsgStatus implements ResponseErrorStatus, Serializable {

    MSG_SEND_PARAM_INVALID(411001, "通知投递参数无效"),
    MSG_ID_INVALID(411002, "通知ID无效"),
    ;

    private final int httpStatus = 200;
    private final Integer status;
    private final String message;
    private final String describe;

    MsgStatus(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.describe = message;
    }

    @Override public int getHttpStatus() { return httpStatus; }
    @Override public String getState() { return name(); }
    @Override public Integer getStatus() { return status; }
    @Override public String getMessage() { return message; }
}
