package com.clmcat.qianyu.mall.his.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

public enum HisStatus implements ResponseErrorStatus, Serializable {

    HIS_BROWSE_HISTORY_NOT_BELONG_USER(411001, "浏览记录不属于当前用户"),
    HIS_BROWSE_HISTORY_NOT_FOUND(411002, "浏览记录不存在"),
    HIS_KEYWORD_TOO_LONG(411003, "搜索关键词超出长度限制"),
    ;

    HisStatus(Integer status, String message) {
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
