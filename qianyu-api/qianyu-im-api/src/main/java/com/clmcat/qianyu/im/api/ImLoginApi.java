package com.clmcat.qianyu.im.api;

import com.clmcat.qianyu.im.api.model.dto.ImLoginResultDto;

/**
 * IM 登录 RPC 接口
 * 供其他服务（如 user-service）通过 Dubbo 调用获取 IM 登录凭证
 */
public interface ImLoginApi {

    /**
     * 为指定用户生成 IM 登录凭证
     *
     * @param userId  业务系统用户 ID
     * @param channel IM 厂商标识: tencent / easemob / rongcloud / nim
     * @return IM 登录结果（含 imToken 和 channel）
     */
    ImLoginResultDto generateImToken(long userId, String channel);

    /**
     * 刷新指定用户的 IM 登录凭证
     *
     * @param userId  业务系统用户 ID
     * @param channel IM 厂商标识
     * @return 新的 IM 登录凭证
     */
    ImLoginResultDto refreshImToken(long userId, String channel);
}
