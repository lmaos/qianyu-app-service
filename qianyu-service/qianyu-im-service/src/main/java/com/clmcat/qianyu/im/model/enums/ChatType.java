package com.clmcat.qianyu.im.model.enums;

/**
 * 会话类型枚举
 */
public enum ChatType {

    /** 私聊 (1v1) */
    SINGLE(1),
    /** 群聊 */
    GROUP(2),
    /** 系统通知（留作未来扩展） */
    SYSTEM(3);

    private final int code;

    ChatType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 根据 code 获取枚举值
     */
    public static ChatType fromCode(int code) {
        for (ChatType type : values()) {
            if (type.code == code) return type;
        }
        return SINGLE;
    }
}
