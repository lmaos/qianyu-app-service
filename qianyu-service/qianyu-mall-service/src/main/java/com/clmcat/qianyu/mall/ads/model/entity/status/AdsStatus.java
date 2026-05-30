package com.clmcat.qianyu.mall.ads.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus;

import java.io.Serializable;

public enum AdsStatus implements ResponseErrorStatus, Serializable {

    ADS_ADDRESS_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "地址不存在", "地址不存在"),
    ADS_ADDRESS_NOT_BELONG_USER(ResponseStatus.A_ACCESS_DENIED.getStatus(), "地址不属于当前用户", "地址不属于当前用户"),
    ADS_REGION_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "地区信息不存在", "地区信息不存在"),
    ADS_ADDRESS_LIMIT_EXCEED(ResponseStatus.P_VALUE_ERROR.getStatus(), "收货地址数量超过上限（最多 20 个）", "收货地址数量超过上限"),
    ADS_CANNOT_DELETE_DEFAULT(ResponseStatus.P_VALUE_ERROR.getStatus(), "不能删除默认地址，请先设置其他默认地址", "不能删除默认地址"),
    ADS_DETAIL_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "详细地址不能为空", "详细地址不能为空"),
    ;

    AdsStatus(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.describe = message;
    }

    AdsStatus(Integer status, String message, String describe) {
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
