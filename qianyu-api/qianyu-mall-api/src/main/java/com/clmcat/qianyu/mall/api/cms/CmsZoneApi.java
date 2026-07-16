package com.clmcat.qianyu.mall.api.cms;

import com.clmcat.qianyu.mall.api.cms.model.dto.CmsZoneDto;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsZoneProductDto;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;

import java.util.List;

/**
 * CMS 楼层（zone）管理 RPC 接口 — 供运营后台（backstage）跨模块调用。
 * 楼层 CRUD + 楼层商品（手动选品）管理。自动投放由 {@code CmsZoneAutoFillTask} 写入 source=AUTO 行。
 */
public interface CmsZoneApi {

    /** 楼层分页（keyword 模糊匹配 title；status 可选）。 */
    PageResultDTO<CmsZoneDto> zonePage(String keyword, Integer status, int pageNum, int pageSize);

    /** 新建楼层，返回楼层 ID。 */
    Long zoneCreate(CmsZoneDto dto);

    /** 更新楼层（id 必填）。 */
    void zoneUpdate(CmsZoneDto dto);

    /** 删除楼层（逻辑删除）。 */
    void zoneDelete(Long zoneId);

    /** 切换楼层显隐：status 0=显示 1=隐藏。 */
    void zoneSetStatus(Long zoneId, int status);

    /** 某楼层的商品列表（带 SPU 展示信息，按 sort 升序）。 */
    List<CmsZoneProductDto> zoneProductList(Long zoneId);

    /** 楼层加商品（source=手动）。已存在则抛重复错误。 */
    void zoneProductAdd(Long zoneId, Long spuId, Integer sort);

    /** 更新楼层商品的排序/显隐。 */
    void zoneProductUpdate(Long id, Integer sort, Integer status);

    /** 移除楼层商品（逻辑删除；手动/自动均可）。 */
    void zoneProductRemove(Long id);
}
