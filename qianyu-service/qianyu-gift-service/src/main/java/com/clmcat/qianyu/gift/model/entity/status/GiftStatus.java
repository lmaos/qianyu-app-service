package com.clmcat.qianyu.gift.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;

import java.io.Serializable;

/**
 * 礼物模块业务错误码（408001 ~ 408020）。
 *
 * @author ark-home
 * @date 2026-08-07
 */
public enum GiftStatus implements ResponseErrorStatus, Serializable {

    /** 礼物不存在或已下架 */
    GIFT_NOT_FOUND(408001, "礼物不存在或已下架"),
    /** 礼物未解锁 */
    GIFT_NOT_UNLOCKED(408002, "礼物未解锁"),
    /** 背包礼物不足 */
    BACKPACK_INSUFFICIENT(408003, "背包礼物不足"),
    /** 背包礼物已过期 */
    BACKPACK_EXPIRED(408004, "背包礼物已过期"),
    /** 道具不存在 */
    PROP_NOT_FOUND(408005, "道具不存在"),
    /** 道具已过期 */
    PROP_EXPIRED(408006, "道具已过期"),
    /** 道具不可穿戴 */
    PROP_NOT_WEARABLE(408007, "道具不可穿戴"),
    /** 道具不在背包中 */
    PROP_NOT_IN_BACKPACK(408008, "道具不在背包中"),
    /** 同类型已穿戴 */
    SLOT_ALREADY_EQUIPPED(408009, "同类型已穿戴其他道具"),
    /** 礼物已下架 */
    GIFT_DISABLED(408010, "礼物已下架"),
    /** 场景不支持该礼物 */
    SCENE_NOT_SUPPORTED(408011, "该场景不支持此礼物"),
    /** 送礼失败 */
    SEND_FAILED(408012, "送礼失败"),
    ;

    private final int httpStatus = 200;
    private final Integer status;
    private final String message;

    GiftStatus(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

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
