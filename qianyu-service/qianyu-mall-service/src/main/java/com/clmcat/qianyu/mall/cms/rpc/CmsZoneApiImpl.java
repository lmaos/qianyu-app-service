package com.clmcat.qianyu.mall.cms.rpc;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.mall.api.cms.CmsZoneApi;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsZoneDto;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsZoneProductDto;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.cms.mapper.CmsZoneMapper;
import com.clmcat.qianyu.mall.cms.mapper.CmsZoneProductMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsZone;
import com.clmcat.qianyu.mall.cms.model.entity.CmsZoneProduct;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsZoneTableDef.CMS_ZONE;
import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsZoneProductTableDef.CMS_ZONE_PRODUCT;

/**
 * CMS 楼层管理 RPC 实现：楼层 CRUD + 楼层商品（手动选品）管理。供运营后台跨模块调用。
 */
@DubboService
@Service
public class CmsZoneApiImpl implements CmsZoneApi {

    @Resource
    private CmsZoneMapper zoneMapper;
    @Resource
    private CmsZoneProductMapper zoneProductMapper;
    @Resource
    private PmsSpuMapper spuMapper;
    @Resource
    private PmsSupport pmsSupport;

    @Override
    public PageResultDTO<CmsZoneDto> zonePage(String keyword, Integer status, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        QueryWrapper qw = QueryWrapper.create().where(CMS_ZONE.DELETED.eq(0));
        if (keyword != null && !keyword.isBlank()) {
            qw.and(CMS_ZONE.TITLE.like("%" + keyword.trim() + "%"));
        }
        if (status != null) {
            qw.and(CMS_ZONE.STATUS.eq(status));
        }
        qw.orderBy(CMS_ZONE.SORT.asc(), CMS_ZONE.ID.asc());
        Page<CmsZone> page = zoneMapper.paginate(Page.of(pageNum, pageSize), qw);
        List<CmsZoneDto> records = page.getRecords().stream().map(this::toZoneDto).collect(Collectors.toList());
        return PageResultDTO.<CmsZoneDto>builder()
                .records(records).total(page.getTotalRow())
                .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
    }

    @Override
    public Long zoneCreate(CmsZoneDto dto) {
        long id = pmsSupport.nextId();
        long now = pmsSupport.parseTime(id);
        CmsZone z = new CmsZone();
        z.setId(id);
        z.setTitle(dto.getTitle());
        z.setTagText(dto.getTagText());
        z.setMoreText(dto.getMoreText());
        z.setLayoutMode(dto.getLayoutMode() != null ? dto.getLayoutMode() : "double");
        z.setProductCount(dto.getProductCount() != null ? dto.getProductCount() : 4);
        z.setSurfaceBackground(dto.getSurfaceBackground());
        z.setSurfaceShadow(dto.getSurfaceShadow());
        z.setCategoryId(dto.getCategoryId());
        z.setFillMode(dto.getFillMode() != null ? dto.getFillMode() : CmsZone.FILL_MIXED);
        z.setSort(dto.getSort() != null ? dto.getSort() : 0);
        z.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        z.setCreateTime(now);
        z.setUpdateTime(now);
        z.setDeleted(0);
        zoneMapper.insertSelective(z);
        return id;
    }

    @Override
    public void zoneUpdate(CmsZoneDto dto) {
        CmsZone exist = zoneMapper.selectOneById(dto.getId());
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx("楼层不存在", exist == null);
        CmsZone u = new CmsZone();
        u.setId(dto.getId());
        if (dto.getTitle() != null) u.setTitle(dto.getTitle());
        if (dto.getTagText() != null) u.setTagText(dto.getTagText());
        if (dto.getMoreText() != null) u.setMoreText(dto.getMoreText());
        if (dto.getLayoutMode() != null) u.setLayoutMode(dto.getLayoutMode());
        if (dto.getProductCount() != null) u.setProductCount(dto.getProductCount());
        if (dto.getSurfaceBackground() != null) u.setSurfaceBackground(dto.getSurfaceBackground());
        if (dto.getSurfaceShadow() != null) u.setSurfaceShadow(dto.getSurfaceShadow());
        if (dto.getCategoryId() != null) u.setCategoryId(dto.getCategoryId());
        if (dto.getFillMode() != null) u.setFillMode(dto.getFillMode());
        if (dto.getSort() != null) u.setSort(dto.getSort());
        if (dto.getStatus() != null) u.setStatus(dto.getStatus());
        u.setUpdateTime(System.currentTimeMillis());
        zoneMapper.update(u);
    }

    @Override
    public void zoneDelete(Long zoneId) {
        CmsZone u = new CmsZone();
        u.setId(zoneId);
        u.setDeleted(1);
        u.setUpdateTime(System.currentTimeMillis());
        zoneMapper.update(u);
    }

    @Override
    public void zoneSetStatus(Long zoneId, int status) {
        CmsZone u = new CmsZone();
        u.setId(zoneId);
        u.setStatus(status);
        u.setUpdateTime(System.currentTimeMillis());
        zoneMapper.update(u);
    }

    @Override
    public List<CmsZoneProductDto> zoneProductList(Long zoneId) {
        List<CmsZoneProduct> rows = zoneProductMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(CMS_ZONE_PRODUCT.ZONE_ID.eq(zoneId))
                        .orderBy(CMS_ZONE_PRODUCT.SORT.asc(), CMS_ZONE_PRODUCT.ID.asc()));
        List<CmsZoneProductDto> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return result;
        }
        // 批量补 SPU 展示信息
        Set<Long> spuIds = new HashSet<>();
        for (CmsZoneProduct r : rows) {
            if (r.getSpuId() != null) spuIds.add(r.getSpuId());
        }
        Map<Long, PmsSpu> spuMap = new HashMap<>();
        if (!spuIds.isEmpty()) {
            List<PmsSpu> spus = spuMapper.selectListByQuery(
                    QueryWrapper.create().where("id in " + inClause(spuIds)));
            for (PmsSpu s : spus) {
                spuMap.put(s.getId(), s);
            }
        }
        for (CmsZoneProduct r : rows) {
            result.add(toProductDto(r, spuMap.get(r.getSpuId())));
        }
        return result;
    }

    @Override
    public void zoneProductAdd(Long zoneId, Long spuId, Integer sort) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx("缺少楼层或商品", zoneId == null || spuId == null);
        long count = zoneProductMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where(CMS_ZONE_PRODUCT.ZONE_ID.eq(zoneId))
                        .and(CMS_ZONE_PRODUCT.SPU_ID.eq(spuId))
                        .and(CMS_ZONE_PRODUCT.DELETED.eq(0)));
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx("该商品已在该楼层中", count > 0);
        long id = pmsSupport.nextId();
        CmsZoneProduct item = new CmsZoneProduct();
        item.setId(id);
        item.setZoneId(zoneId);
        item.setSpuId(spuId);
        item.setSort(sort != null ? sort : 0);
        item.setStatus(CmsZoneProduct.STATUS_SHOW);
        item.setSource(CmsZoneProduct.SOURCE_MANUAL);
        item.setCreateTime(pmsSupport.parseTime(id));
        item.setUpdateTime(System.currentTimeMillis());
        item.setDeleted(0);
        zoneProductMapper.insertSelective(item);
    }

    @Override
    public void zoneProductUpdate(Long id, Integer sort, Integer status) {
        CmsZoneProduct u = new CmsZoneProduct();
        u.setId(id);
        if (sort != null) u.setSort(sort);
        if (status != null) u.setStatus(status);
        u.setUpdateTime(System.currentTimeMillis());
        zoneProductMapper.update(u);
    }

    @Override
    public void zoneProductRemove(Long id) {
        CmsZoneProduct u = new CmsZoneProduct();
        u.setId(id);
        u.setDeleted(1);
        u.setUpdateTime(System.currentTimeMillis());
        zoneProductMapper.update(u);
    }

    // ==================== helpers ====================

    private CmsZoneDto toZoneDto(CmsZone z) {
        CmsZoneDto d = new CmsZoneDto();
        d.setId(z.getId());
        d.setTitle(z.getTitle());
        d.setTagText(z.getTagText());
        d.setMoreText(z.getMoreText());
        d.setLayoutMode(z.getLayoutMode());
        d.setProductCount(z.getProductCount());
        d.setSurfaceBackground(z.getSurfaceBackground());
        d.setSurfaceShadow(z.getSurfaceShadow());
        d.setCategoryId(z.getCategoryId());
        d.setFillMode(z.getFillMode());
        d.setSort(z.getSort());
        d.setStatus(z.getStatus());
        d.setCreateTime(z.getCreateTime());
        return d;
    }

    private CmsZoneProductDto toProductDto(CmsZoneProduct r, PmsSpu spu) {
        CmsZoneProductDto d = new CmsZoneProductDto();
        d.setId(r.getId());
        d.setZoneId(r.getZoneId());
        d.setSpuId(r.getSpuId());
        d.setSort(r.getSort());
        d.setStatus(r.getStatus());
        d.setSource(r.getSource());
        if (spu != null) {
            d.setSpuName(spu.getName());
            d.setSpuMainImage(spu.getMainImage());
            d.setSpuStatus(spu.getStatus());
            d.setSpuPrice(spu.getMinPrice() != null ? spu.getMinPrice().toPlainString() : null);
            d.setSpuMerchantId(spu.getMerchantId());
        }
        return d;
    }

    private String inClause(Set<Long> ids) {
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (Long id : ids) {
            if (!first) sb.append(",");
            sb.append(id);
            first = false;
        }
        return sb.append(")").toString();
    }
}
