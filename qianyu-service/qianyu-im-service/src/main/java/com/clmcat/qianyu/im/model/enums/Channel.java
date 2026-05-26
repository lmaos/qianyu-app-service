package com.clmcat.qianyu.im.model.enums;

/**
 * IM 厂商渠道枚举
 */
public enum Channel {

    /** 腾讯云 IM */
    TENCENT("tencent"),
    /** 环信 IM */
    EASEMOB("easemob"),
    /** 融云 IM */
    RONGCLOUD("rongcloud"),
    /** 网易云信 IM */
    NIM("nim");

    private final String code;

    Channel(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 根据 code 获取枚举值
     */
    public static Channel fromCode(String code) {
        for (Channel ch : values()) {
            if (ch.code.equals(code)) return ch;
        }
        return TENCENT;
    }
}
