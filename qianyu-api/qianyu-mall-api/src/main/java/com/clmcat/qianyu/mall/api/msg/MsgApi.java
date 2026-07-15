package com.clmcat.qianyu.mall.api.msg;

/**
 * 系统通知投递 Dubbo 契约（跨模块投递用，如商户审核结果）。
 * <p>C 端拉取/已读由 {@code MsgViewServiceBiz} 提供（同模块直调 {@code MsgApiImpl}）。
 */
public interface MsgApi {

    /**
     * 投递一条系统通知。
     *
     * @param userId   接收用户ID（&gt;0）
     * @param type     通知类型：1=商户 2=订单 3=支付 4=售后 5=系统
     * @param title    标题（非空）
     * @param content  正文（可为空串；如审核拒绝理由）
     * @param bizType  业务类型（跳转用，如 merchant_audit；可空串）
     * @param bizId    业务ID（跳转用，如 merchantId；可 0）
     * @return 通知 messageId
     */
    long send(Long userId, Integer type, String title, String content, String bizType, Long bizId);
}
