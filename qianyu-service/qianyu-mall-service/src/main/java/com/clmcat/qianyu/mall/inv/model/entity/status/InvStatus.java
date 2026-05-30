package com.clmcat.qianyu.mall.inv.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

public enum InvStatus implements ResponseErrorStatus, Serializable {

    INV_SKU_NOT_FOUND(404001, "SKU 库存记录不存在"),
    INV_STOCK_NOT_ENOUGH(404002, "可用库存不足"),
    INV_LOCK_NOT_FOUND(404003, "锁定记录不存在（订单未锁定）"),
    INV_LOCK_ALREADY_CONFIRMED(404004, "锁定已确认，不可重复操作"),
    INV_LOCK_ALREADY_RELEASED(404005, "锁定已释放，不可重复操作"),
    INV_ADJUST_QUANTITY_INVALID(404006, "调整数量无效（减少后库存为负）"),
    INV_SKU_NOT_BELONG_MERCHANT(404007, "SKU 不属于当前商家"),
    INV_BATCH_QUERY_LIMIT_EXCEED(404008, "批量查询超过上限（100）"),
    INV_OPTIMISTIC_LOCK_FAIL(404009, "库存并发冲突，请重试"),
    ;

    InvStatus(Integer status, String message) {
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
