package com.clmcat.qianyu.mall.log.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

public enum LogisticsStatus implements ResponseErrorStatus, Serializable {

    LOG_LOGISTICS_NOT_FOUND(406001, "物流单不存在"),
    LOG_LOGISTICS_NOT_BELONG_USER(406002, "物流单不属于当前用户"),
    LOG_LOGISTICS_NOT_BELONG_MERCHANT(406003, "物流单不属于当前商家"),
    LOG_ORDER_ALREADY_SHIPPED(406004, "订单已发货，不可重复创建物流单"),
    LOG_LOGISTICS_NO_INVALID(406005, "物流单号格式不正确"),
    LOG_LOGISTICS_CODE_INVALID(406006, "物流公司编码不支持"),
    LOG_TRACK_QUERY_FAIL(406007, "物流轨迹查询失败（物流公司接口异常）"),
    LOG_SIGN_VERIFY_FAIL(406008, "物流推送签名验证失败"),
    LOG_LOGISTICS_ALREADY_SIGNED(406009, "物流已签收，不可修改"),
    ;

    LogisticsStatus(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.describe = message;
    }

    private final int httpStatus = 200;
    private final Integer status;
    private final String message;
    private final String describe;

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
