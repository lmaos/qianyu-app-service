package com.clmcat.qianyu.im.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * IM 登录请求 DTO
 * 前端请求 /api/im/login 时提交
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ImLoginDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** IM 厂商标识: tencent / easemob / rongcloud / nim */
    private String channel;

    /** 用户 ID（未登录时由前端传入，已登录时使用 @Token） */
    private Long userId;
}
