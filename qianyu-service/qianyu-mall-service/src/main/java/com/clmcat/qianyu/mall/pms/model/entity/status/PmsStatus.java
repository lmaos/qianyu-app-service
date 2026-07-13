package com.clmcat.qianyu.mall.pms.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus;

import java.io.Serializable;

public enum PmsStatus implements ResponseErrorStatus, Serializable {

    PMS_SPU_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "SPU 不存在"),
    PMS_SPU_OFF_SHELF(ResponseStatus.R_OPERATION_FAIL.getStatus(), "SPU 已下架"),
    PMS_SPU_NOT_OWNER(ResponseStatus.A_ACCESS_DENIED.getStatus(), "非本商家商品，无权操作"),
    PMS_SKU_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "SKU 不存在"),
    PMS_SKU_STOCK_NOT_ENOUGH(ResponseStatus.R_OPERATION_FAIL.getStatus(), "SKU 库存不足"),
    PMS_CATEGORY_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "分类不存在"),
    PMS_CATEGORY_HAS_CHILDREN(ResponseStatus.R_OPERATION_FAIL.getStatus(), "分类下有子分类，无法删除"),
    PMS_CATEGORY_HAS_PRODUCTS(ResponseStatus.R_OPERATION_FAIL.getStatus(), "分类下有商品，无法删除"),
    PMS_BRAND_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "品牌不存在"),
    PMS_BRAND_NAME_DUPLICATE(ResponseStatus.P_VALUE_ERROR.getStatus(), "品牌名称重复"),
    PMS_CATEGORY_NAME_DUPLICATE(ResponseStatus.P_VALUE_ERROR.getStatus(), "同级下分类名称重复"),
    PMS_SPU_PARAM_INVALID(ResponseStatus.P_VALUE_ERROR.getStatus(), "商品参数校验失败"),
    PMS_SKU_PRICE_INVALID(ResponseStatus.P_VALUE_ERROR.getStatus(), "SKU 价格无效（须大于 0）"),
    PMS_SPU_NOT_AUDIT_PASSED(ResponseStatus.A_ACCESS_DENIED.getStatus(), "商品未通过审核，无法上架"),
    PMS_SPU_STATUS_INVALID(ResponseStatus.P_VALUE_ERROR.getStatus(), "当前商品状态不允许该操作"),
    PMS_MERCHANT_NOT_FOUND(ResponseStatus.A_ACCESS_DENIED.getStatus(), "非商家用户，无权操作商品"),
    ;

    PmsStatus(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.describe = message;
    }

    PmsStatus(Integer status, String message, String describe) {
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
