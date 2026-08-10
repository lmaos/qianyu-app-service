package com.clmcat.qianyu.gift.rule;

import com.clmcat.framework.webmvc.ResponseStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 礼物类型处理器注册表。
 * <p>
 * Spring 自动发现所有 {@link GiftTypeHandler} 实现并注册。
 * 送礼主流程通过此注册表获取对应处理器，完全不感知具体 Handler 实现。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Component
public class GiftTypeHandlerRegistry {

    private final Map<Integer, GiftTypeHandler> handlers;

    public GiftTypeHandlerRegistry(List<GiftTypeHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(GiftTypeHandler::giftType, Function.identity()));
    }

    /**
     * 根据礼物类型获取处理器。
     *
     * @param giftType 礼物类型
     * @return 处理器，未找到返回 null
     */
    public GiftTypeHandler getHandler(int giftType) {
        return handlers.get(giftType);
    }

    /**
     * 获取处理器，如果未找到则抛异常。
     */
    public GiftTypeHandler getRequiredHandler(int giftType) {
        GiftTypeHandler handler = handlers.get(giftType);
        if (handler == null) {
            ResponseStatus.P_VALUE_ERROR.assertThrowResEx(true, "不支持的礼物类型: " + giftType);
        }
        return handler;
    }
}
