package com.clmcat.qianyu.mall.rev.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

public enum RevStatus implements ResponseErrorStatus, Serializable {

    REV_REVIEW_NOT_FOUND(408001, "评价不存在"),
    REV_ALREADY_REVIEWED(408002, "该商品已评价，不可重复提交"),
    REV_ORDER_NOT_COMPLETED(408003, "订单未完成，不可评价"),
    REV_REVIEW_NOT_BELONG_USER(408004, "评价不属于当前用户"),
    REV_REVIEW_NOT_BELONG_MERCHANT(408005, "评价不属于当前商家商品"),
    REV_ALREADY_REPLIED(408006, "该评价已回复，不可重复回复"),
    REV_CONTENT_TOO_LONG(408007, "评价内容超出字数限制"),
    REV_IMAGE_LIMIT_EXCEED(408008, "评价图片超过 9 张"),
    REV_SCORE_INVALID(408009, "评分无效（须为 1-5 整数）"),
    REV_REPLY_CONTENT_TOO_LONG(408010, "回复内容超出字数限制"),
    ;

    RevStatus(Integer status, String message) {
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
