package com.clmcat.qianyu.mall.cms.service;

import com.clmcat.qianyu.mall.cms.model.entity.CmsZone;

import java.util.List;

/**
 * CMS Zone 数据服务接口
 */
public interface CmsZoneServiceBiz {

    /**
     * 查询所有启用的 Zone 列表（按 sort 升序）
     */
    List<CmsZone> selectAllEnabled();
}
