package com.clmcat.qianyu.payment.wallet.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

/**
 * 支付/钱包模块自定义业务状态。
 *
 * @author ark-home
 * @date 2026-08-03
 */
public enum PaymentStatus implements ResponseErrorStatus, Serializable {

    WALLET_NOT_FOUND(407001, "钱包不存在"),
    TRANSACTION_NOT_FOUND(407002, "交易流水不存在"),
    ;

    PaymentStatus(Integer status, String message) {
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
