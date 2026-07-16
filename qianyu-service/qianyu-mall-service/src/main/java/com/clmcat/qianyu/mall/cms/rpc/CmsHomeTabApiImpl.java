package com.clmcat.qianyu.mall.cms.rpc;

import com.clmcat.qianyu.mall.api.cms.CmsHomeTabApi;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsHomeTabDto;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.cms.mapper.CmsHomeTabMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsHomeTab;
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

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsHomeTabTableDef.CMS_HOME_TAB;

/**
 * CMS HomeTab 管理服务（RPC）：设默认 + CRUD（CUD 后刷新 C 端 Tab/Banner 缓存）。
 */
@DubboService
@Service
public class CmsHomeTabApiImpl implements CmsHomeTabApi {

    @Resource
    private CmsHomeTabMapper homeTabMapper;
    @Resource
    private PmsSupport pmsSupport;
    @Lazy
    @Resource
    private CmsViewBizImpl cmsViewBiz;

    @Override
    public void setDefault(Long tabId) {
        // 1. 取消所有 Tab 的默认
        List<CmsHomeTab> allTabs = homeTabMapper.selectListByQuery(
                QueryWrapper.create().where(CMS_HOME_TAB.IS_DEFAULT.eq(1))
        );
        for (CmsHomeTab tab : allTabs) {
            CmsHomeTab update = new CmsHomeTab();
            update.setId(tab.getId());
            update.setIsDefault(0);
            homeTabMapper.update(update);
        }
        // 2. 设置指定 Tab 为默认
        CmsHomeTab update = new CmsHomeTab();
        update.setId(tabId);
        update.setIsDefault(1);
        homeTabMapper.update(update);
        refreshCacheSafe();
    }

    @Override
    public PageResultDTO<CmsHomeTabDto> page(String keyword, Integer status, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        QueryWrapper qw = QueryWrapper.create().where(CMS_HOME_TAB.DELETED.eq(0));
        if (keyword != null && !keyword.isBlank()) {
            qw.and(CMS_HOME_TAB.NAME.like("%" + keyword.trim() + "%"));
        }
        if (status != null) {
            qw.and(CMS_HOME_TAB.STATUS.eq(status));
        }
        qw.orderBy(CMS_HOME_TAB.SORT.asc(), CMS_HOME_TAB.ID.asc());
        Page<CmsHomeTab> page = homeTabMapper.paginate(Page.of(pageNum, pageSize), qw);
        List<CmsHomeTabDto> records = page.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        return PageResultDTO.<CmsHomeTabDto>builder()
                .records(records).total(page.getTotalRow())
                .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
    }

    @Override
    public Long create(CmsHomeTabDto dto) {
        long id = pmsSupport.nextId();
        long now = pmsSupport.parseTime(id);
        CmsHomeTab t = new CmsHomeTab();
        t.setId(id);
        t.setName(dto.getName());
        t.setTabKey(dto.getTabKey());
        t.setCategoryId(dto.getCategoryId());
        t.setIcon(dto.getIcon());
        t.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        t.setSort(dto.getSort() != null ? dto.getSort() : 0);
        t.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        t.setCreateTime(now);
        t.setUpdateTime(now);
        t.setDeleted(0);
        homeTabMapper.insertSelective(t);
        refreshCacheSafe();
        return id;
    }

    @Override
    public void update(CmsHomeTabDto dto) {
        CmsHomeTab u = new CmsHomeTab();
        u.setId(dto.getId());
        if (dto.getName() != null) u.setName(dto.getName());
        if (dto.getTabKey() != null) u.setTabKey(dto.getTabKey());
        if (dto.getCategoryId() != null) u.setCategoryId(dto.getCategoryId());
        if (dto.getIcon() != null) u.setIcon(dto.getIcon());
        if (dto.getIsDefault() != null) u.setIsDefault(dto.getIsDefault());
        if (dto.getSort() != null) u.setSort(dto.getSort());
        if (dto.getStatus() != null) u.setStatus(dto.getStatus());
        u.setUpdateTime(System.currentTimeMillis());
        homeTabMapper.update(u);
        refreshCacheSafe();
    }

    @Override
    public void delete(Long tabId) {
        CmsHomeTab u = new CmsHomeTab();
        u.setId(tabId);
        u.setDeleted(1);
        u.setUpdateTime(System.currentTimeMillis());
        homeTabMapper.update(u);
        refreshCacheSafe();
    }

    private void refreshCacheSafe() {
        try {
            if (cmsViewBiz != null) {
                cmsViewBiz.refreshTabAndBannerCache();
            }
        } catch (Exception ignored) {
            // 缓存刷新失败不阻断主流程（@Scheduled 每 30s 兜底）
        }
    }

    private CmsHomeTabDto toDto(CmsHomeTab t) {
        CmsHomeTabDto d = new CmsHomeTabDto();
        d.setId(t.getId());
        d.setName(t.getName());
        d.setTabKey(t.getTabKey());
        d.setCategoryId(t.getCategoryId());
        d.setIcon(t.getIcon());
        d.setIsDefault(t.getIsDefault());
        d.setSort(t.getSort());
        d.setStatus(t.getStatus());
        d.setCreateTime(t.getCreateTime());
        return d;
    }
}
