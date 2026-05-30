package com.clmcat.qianyu.mall.mch.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

public enum MchStatus implements ResponseErrorStatus, Serializable {

    MCH_MERCHANT_NOT_FOUND(407001, "商家不存在", "商家不存在"),
    MCH_MERCHANT_FROZEN(407002, "商家已被冻结", "商家已被冻结"),
    MCH_MERCHANT_NOT_APPROVED(407003, "商家审核未通过", "商家审核未通过"),
    MCH_SHOP_NAME_DUPLICATE(407004, "店铺名称重复", "店铺名称重复"),
    MCH_ALREADY_MERCHANT(407005, "当前用户已是商家，不可重复申请", "当前用户已是商家"),
    MCH_SETTLE_APPLY_NOT_FOUND(407006, "入驻申请不存在", "入驻申请不存在"),
    MCH_SETTLE_ALREADY_APPLIED(407007, "已提交入驻申请，请勿重复提交", "已提交入驻申请"),
    MCH_BUSINESS_LICENSE_DUPLICATE(407008, "营业执照编号已被使用", "营业执照编号已被使用"),
    MCH_NOT_MERCHANT(407009, "当前用户不是商家", "当前用户不是商家"),
    MCH_SETTLE_AUDIT_FAIL(407010, "审核操作失败", "审核操作失败"),
    MCH_INSUFFICIENT_BALANCE(407011, "账户余额不足", "账户余额不足"),
    MCH_WITHDRAW_AMOUNT_INVALID(407012, "提现金额无效", "提现金额无效"),
    MCH_FREIGHT_TEMPLATE_NOT_FOUND(407013, "运费模板不存在", "运费模板不存在"),
    MCH_FREIGHT_TEMPLATE_IN_USE(407014, "运费模板正在使用中，无法删除", "运费模板正在使用中"),
    MCH_SETTLEMENT_NOT_FOUND(407015, "结算单不存在", "结算单不存在"),
    MCH_BILL_NOT_FOUND(407016, "账单不存在", "账单不存在"),
    ;

    MchStatus(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.describe = message;
    }

    MchStatus(Integer status, String message, String describe) {
        this.status = status;
        this.message = message;
        this.describe = describe;
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
