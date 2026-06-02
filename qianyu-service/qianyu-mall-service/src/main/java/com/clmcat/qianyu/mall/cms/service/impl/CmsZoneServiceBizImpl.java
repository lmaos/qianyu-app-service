package com.clmcat.qianyu.mall.cms.service.impl;

import com.clmcat.qianyu.mall.cms.mapper.CmsZoneMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsZone;
import com.clmcat.qianyu.mall.cms.service.CmsZoneServiceBiz;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsZoneTableDef.CMS_ZONE;

@Service
public class CmsZoneServiceBizImpl implements CmsZoneServiceBiz {

    @Resource
    private CmsZoneMapper zoneMapper;

    @Override
    public List<CmsZone> selectAllEnabled() {
        List<CmsZone> zones = zoneMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(CMS_ZONE.STATUS.eq(0))
                        .orderBy(CMS_ZONE.SORT.asc())
        );
        return zones != null ? zones : Collections.emptyList();
    }
}
