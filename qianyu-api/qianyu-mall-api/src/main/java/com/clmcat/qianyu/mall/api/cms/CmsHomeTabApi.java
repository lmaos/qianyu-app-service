package com.clmcat.qianyu.mall.api.cms;

import com.clmcat.qianyu.mall.api.cms.model.dto.CmsHomeTabDto;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;

/**
 * CMS HomeTab RPC 接口 — 管理端操作（首页导航 Tab CRUD + 设默认）。
 */
public interface CmsHomeTabApi {

    /** 设置默认 Tab */
    void setDefault(Long tabId);

    /** Tab 分页（keyword 模糊 name；status 可选）。 */
    PageResultDTO<CmsHomeTabDto> page(String keyword, Integer status, int pageNum, int pageSize);

    /** 新建 Tab，返回 ID。 */
    Long create(CmsHomeTabDto dto);

    /** 更新 Tab（id 必填）。 */
    void update(CmsHomeTabDto dto);

    /** 删除 Tab（逻辑删除）。 */
    void delete(Long tabId);
}
