package com.clmcat.qianyu.im.provider;

import com.clmcat.qianyu.im.model.body.MessageBody;

/**
 * IM 厂商 Provider 接口
 * 每个厂商实现此接口，负责消息发送和登录凭证生成
 */
public interface IMProvider {

    /**
     * 发送消息
     * 将统一消息体转换为厂商 REST API 格式并调用
     *
     * @param body 统一消息体
     * @return 厂商响应 JSON
     */
    String sendMessage(MessageBody body);

    /**
     * 为用户生成 IM 登录凭证
     * 腾讯云: UserSig
     * 环信: 动态 Token
     * 融云: Token (通过 getToken API)
     * 网易云信: Token (通过 create.action)
     *
     * @param userId 业务系统用户 ID
     * @return IM 登录凭证
     */
    String generateToken(long userId);

    /**
     * 刷新 IM 登录凭证
     *
     * @param userId 业务系统用户 ID
     * @return 新的 IM 登录凭证
     */
    String refreshToken(long userId);

    /**
     * 确保用户在厂商平台已注册（不存在则创建）
     *
     * @param userId 业务系统用户 ID
     */
    void ensureUserRegistered(long userId);

    /**
     * 返回此 Provider 负责的渠道标识
     *
     * @return 渠道: tencent / easemob / rongcloud / nim
     */
    String getChannel();

    /**
     * 返回厂商 SDK 应用 ID
     * 客户端初始化 SDK 时需要此值
     *
     * @return SDK 应用 ID，无则返回 0
     */
    default long getSdkAppId() {
        return 0;
    }
}
