package com.clmcat.qianyu.mall.pay.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

public enum PayStatus implements ResponseErrorStatus, Serializable {

    PAY_ORDER_NOT_FOUND(403001, "支付单不存在"),
    PAY_ORDER_ALREADY_PAID(403002, "支付单已支付，请勿重复支付"),
    PAY_ORDER_CLOSED(403003, "支付单已关闭（超时未支付）"),
    PAY_CHANNEL_NOT_SUPPORT(403004, "不支持的支付渠道"),
    PAY_AMOUNT_MISMATCH(403005, "支付金额与订单金额不一致"),
    PAY_SIGN_VERIFY_FAIL(403006, "回调签名验证失败"),
    PAY_REFUND_AMOUNT_EXCEED(403007, "退款金额超过支付金额"),
    PAY_REFUND_FAIL(403008, "退款失败（渠道返回错误）"),
    PAY_CALL_CHANNEL_FAIL(403009, "调用支付渠道失败（网络/渠道异常）"),
    PAY_ORDER_STATUS_ERROR(403010, "支付单状态不允许此操作"),
    PAY_SANDBOX_FAIL(403011, "沙箱支付失败"),
    ;

    PayStatus(Integer status, String message) {
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
