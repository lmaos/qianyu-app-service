package com.clmcat.qianyu.mall.oms.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

public enum OmsStatus implements ResponseErrorStatus, Serializable {

    OMS_CART_NOT_FOUND(405001, "购物车项不存在"),
    OMS_ORDER_NOT_FOUND(405002, "订单不存在"),
    OMS_ORDER_STATUS_ERROR(405003, "订单状态不允许此操作"),
    OMS_ORDER_NOT_BELONG_USER(405004, "非本用户订单"),
    OMS_CART_QUANTITY_INVALID(405005, "购物车数量无效"),
    OMS_ORDER_ALREADY_CANCELLED(405006, "订单已取消"),
    OMS_ORDER_ALREADY_COMPLETED(405007, "订单已完成"),
    OMS_AFTERSALE_NOT_FOUND(405008, "售后单不存在"),
    OMS_AFTERSALE_STATUS_ERROR(405009, "售后状态不允许此操作"),
    OMS_AFTERSALE_NOT_BELONG_USER(405010, "非本用户售后单"),
    OMS_ADDRESS_REQUIRED(405011, "收货地址不能为空"),
    OMS_ORDER_ITEMS_EMPTY(405012, "订单商品不能为空"),
    OMS_ORDER_NOT_BELONG_MERCHANT(405013, "非本商户订单"),
    OMS_AFTERSALE_NOT_BELONG_MERCHANT(405014, "非本商户售后单"),
    OMS_SKU_NOT_FOUND(405015, "商品SKU不存在"),
    OMS_ADDRESS_NOT_FOUND(405016, "收货地址不存在"),
    OMS_ADDRESS_NOT_BELONG_USER(405017, "收货地址不属于当前用户"),
    OMS_STOCK_LOCK_FAILED(405018, "库存锁定失败"),
    OMS_ORDER_DUPLICATE_REQUEST(405019, "订单重复提交，请勿重复操作"),
    ;

    OmsStatus(Integer status, String message) {
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
