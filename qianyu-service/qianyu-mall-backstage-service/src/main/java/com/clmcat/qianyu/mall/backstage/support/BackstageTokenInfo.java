package com.clmcat.qianyu.mall.backstage.support;

import com.clmcat.framework.webmvc.verify.TokenInfo;

/**
 * 运营后台 TokenInfo：供框架 {@code @Token Long adminId} 注入器消费。
 * getUserId() 在 backstage 域语义为 adminId（运营账号 ID）。
 */
public class BackstageTokenInfo implements TokenInfo {

    private final Long adminId;
    private final boolean invalid;

    public BackstageTokenInfo(Long adminId) {
        this(adminId, adminId == null);
    }

    public BackstageTokenInfo(Long adminId, boolean invalid) {
        this.adminId = adminId;
        this.invalid = invalid;
    }

    @Override
    public Long getUserId() {
        return adminId;
    }

    @Override
    public boolean isInvalid() {
        return invalid;
    }
}
