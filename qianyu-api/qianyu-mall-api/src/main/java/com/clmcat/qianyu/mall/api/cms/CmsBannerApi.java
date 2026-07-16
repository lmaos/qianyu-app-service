package com.clmcat.qianyu.mall.api.cms;

import com.clmcat.qianyu.mall.api.cms.model.dto.CmsBannerDto;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;

/**
 * CMS Banner RPC 接口 — 管理端操作（首页轮播 Banner CRUD）。
 */
public interface CmsBannerApi {

    /** Banner 分页（keyword 模糊 title；status 可选）。 */
    PageResultDTO<CmsBannerDto> page(String keyword, Integer status, int pageNum, int pageSize);

    /** 新建 Banner，返回 ID。 */
    Long create(CmsBannerDto dto);

    /** 更新 Banner（id 必填）。 */
    void update(CmsBannerDto dto);

    /** 删除 Banner（逻辑删除）。 */
    void delete(Long bannerId);
}
