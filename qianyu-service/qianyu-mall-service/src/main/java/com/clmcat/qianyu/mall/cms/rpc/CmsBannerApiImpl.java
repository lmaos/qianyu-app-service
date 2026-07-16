package com.clmcat.qianyu.mall.cms.rpc;

import com.clmcat.qianyu.mall.api.cms.CmsBannerApi;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsBannerDto;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.cms.mapper.CmsBannerMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsBanner;
import com.clmcat.qianyu.mall.cms.service.impl.CmsViewBizImpl;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsBannerTableDef.CMS_BANNER;

/**
 * CMS Banner 管理服务（RPC）：轮播 CRUD（CUD 后刷新 C 端 Tab/Banner 缓存）。
 */
@DubboService
@Service
public class CmsBannerApiImpl implements CmsBannerApi {

    @Resource
    private CmsBannerMapper bannerMapper;
    @Resource
    private PmsSupport pmsSupport;
    @Lazy
    @Resource
    private CmsViewBizImpl cmsViewBiz;

    @Override
    public PageResultDTO<CmsBannerDto> page(String keyword, Integer status, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        QueryWrapper qw = QueryWrapper.create().where(CMS_BANNER.DELETED.eq(0));
        if (keyword != null && !keyword.isBlank()) {
            qw.and(CMS_BANNER.TITLE.like("%" + keyword.trim() + "%"));
        }
        if (status != null) {
            qw.and(CMS_BANNER.STATUS.eq(status));
        }
        qw.orderBy(CMS_BANNER.SORT.asc(), CMS_BANNER.ID.asc());
        Page<CmsBanner> page = bannerMapper.paginate(Page.of(pageNum, pageSize), qw);
        List<CmsBannerDto> records = page.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        return PageResultDTO.<CmsBannerDto>builder()
                .records(records).total(page.getTotalRow())
                .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
    }

    @Override
    public Long create(CmsBannerDto dto) {
        long id = pmsSupport.nextId();
        long now = pmsSupport.parseTime(id);
        CmsBanner b = new CmsBanner();
        b.setId(id);
        applyFields(b, dto);
        b.setSort(dto.getSort() != null ? dto.getSort() : 0);
        b.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        b.setCreateTime(now);
        b.setUpdateTime(now);
        b.setDeleted(0);
        bannerMapper.insertSelective(b);
        refreshCacheSafe();
        return id;
    }

    @Override
    public void update(CmsBannerDto dto) {
        CmsBanner u = new CmsBanner();
        u.setId(dto.getId());
        applyFields(u, dto);
        if (dto.getSort() != null) u.setSort(dto.getSort());
        if (dto.getStatus() != null) u.setStatus(dto.getStatus());
        u.setUpdateTime(System.currentTimeMillis());
        bannerMapper.update(u);
        refreshCacheSafe();
    }

    @Override
    public void delete(Long bannerId) {
        CmsBanner u = new CmsBanner();
        u.setId(bannerId);
        u.setDeleted(1);
        u.setUpdateTime(System.currentTimeMillis());
        bannerMapper.update(u);
        refreshCacheSafe();
    }

    private void applyFields(CmsBanner b, CmsBannerDto dto) {
        if (dto.getTitle() != null) b.setTitle(dto.getTitle());
        if (dto.getDescription() != null) b.setDescription(dto.getDescription());
        if (dto.getActionText() != null) b.setActionText(dto.getActionText());
        if (dto.getTagText() != null) b.setTagText(dto.getTagText());
        if (dto.getImage() != null) b.setImage(dto.getImage());
        if (dto.getLinkUrl() != null) b.setLinkUrl(dto.getLinkUrl());
        if (dto.getLinkType() != null) b.setLinkType(dto.getLinkType());
        if (dto.getLinkValue() != null) b.setLinkValue(dto.getLinkValue());
    }

    private void refreshCacheSafe() {
        try {
            if (cmsViewBiz != null) {
                cmsViewBiz.refreshTabAndBannerCache();
            }
        } catch (Exception ignored) {
        }
    }

    private CmsBannerDto toDto(CmsBanner b) {
        CmsBannerDto d = new CmsBannerDto();
        d.setId(b.getId());
        d.setTitle(b.getTitle());
        d.setDescription(b.getDescription());
        d.setActionText(b.getActionText());
        d.setTagText(b.getTagText());
        d.setImage(b.getImage());
        d.setLinkUrl(b.getLinkUrl());
        d.setLinkType(b.getLinkType());
        d.setLinkValue(b.getLinkValue());
        d.setSort(b.getSort());
        d.setStatus(b.getStatus());
        d.setCreateTime(b.getCreateTime());
        return d;
    }
}
