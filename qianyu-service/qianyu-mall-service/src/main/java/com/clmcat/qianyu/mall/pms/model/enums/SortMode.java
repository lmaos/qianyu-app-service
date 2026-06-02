package com.clmcat.qianyu.mall.pms.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商品排序方式
 */
@Getter
@RequiredArgsConstructor
public enum SortMode {

    RECOMMEND("recommend", "综合排序"),
    SALES("sales", "销量排序"),
    PRICE("price", "价格排序");

    private final String value;
    private final String label;

    /**
     * 根据字符串值解析枚举，无法匹配时默认返回 RECOMMEND
     */
    public static SortMode of(String value) {
        if (value == null) {
            return RECOMMEND;
        }
        for (SortMode mode : values()) {
            if (mode.value.equals(value)) {
                return mode;
            }
        }
        return RECOMMEND;
    }
}
