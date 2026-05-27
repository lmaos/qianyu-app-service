package com.clmcat.qianyu.im.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * IM 登录结果 DTO
 * 包含厂商标识和该用户的 IM 登录凭证
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ImLoginResultDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** IM 登录凭证（腾讯云: UserSig, 环信/融云/网易云信: Token） */
    private String imToken;

    /** IM 厂商标识: tencent / easemob / rongcloud / nim */
    private String channel;

    /** 厂商 SDK 应用 ID（客户端初始化 SDK 需要） */
    private Long sdkAppId;
}
