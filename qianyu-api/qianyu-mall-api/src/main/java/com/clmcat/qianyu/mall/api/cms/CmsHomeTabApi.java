package com.clmcat.qianyu.mall.api.cms;

/**
 * CMS HomeTab RPC 接口 — 管理端操作
 */
public interface CmsHomeTabApi {

    /**
     * 设置默认 Tab
     *
     * @param tabId 要设为默认的 Tab ID
     */
    void setDefault(Long tabId);
}
