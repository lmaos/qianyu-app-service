package com.clmcat.qianyu.storage.model.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * 预签名链接结果
 */
@Getter
@Builder
public class PresignResultVo {

    /** 预签名访问 URL */
    private String url;

    /** 有效期（秒） */
    private long expireSeconds;
}
