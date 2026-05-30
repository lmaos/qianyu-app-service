package com.clmcat.qianyu.mall.fav.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

public enum FavStatus implements ResponseErrorStatus, Serializable {

    FAV_TARGET_NOT_FOUND(410001, "收藏目标不存在（商品/店铺不存在）"),
    FAV_ALREADY_EXISTS(410002, "已收藏该目标（幂等处理，不视为错误）"),
    FAV_NOT_FOUND(410003, "收藏记录不存在（取消收藏时）"),
    FAV_TYPE_INVALID(410004, "收藏类型无效（须为 1 或 2）"),
    FAV_NOT_BELONG_USER(410005, "收藏记录不属于当前用户"),
    FAV_BATCH_LIMIT_EXCEED(410006, "批量操作超过上限（50 条）"),
    FAV_SPU_OFF_SHELF(410007, "商品已下架（收藏列表中标记，不阻止收藏行为）"),
    ;

    FavStatus(Integer status, String message) {
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
