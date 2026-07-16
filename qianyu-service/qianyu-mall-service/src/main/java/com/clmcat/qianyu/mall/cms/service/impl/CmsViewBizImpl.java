package com.clmcat.qianyu.mall.cms.service.impl;

import com.clmcat.qianyu.mall.cms.mapper.CmsBannerMapper;
import com.clmcat.qianyu.mall.cms.mapper.CmsHomeTabMapper;
import com.clmcat.qianyu.mall.cms.mapper.CmsZoneProductMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsBanner;
import com.clmcat.qianyu.mall.cms.model.entity.CmsHomeTab;
import com.clmcat.qianyu.mall.cms.model.entity.CmsZone;
import com.clmcat.qianyu.mall.cms.model.entity.CmsZoneProduct;
import com.clmcat.qianyu.mall.cms.service.CmsZoneServiceBiz;
import com.clmcat.qianyu.mall.cms.model.vo.BannerVo;
import com.clmcat.qianyu.mall.cms.model.vo.HomePageVo;
import com.clmcat.qianyu.mall.cms.model.vo.HomeTabVo;
import com.clmcat.qianyu.mall.cms.model.vo.TabZoneListVo;
import com.clmcat.qianyu.mall.cms.model.vo.ZoneVo;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsHomeTabTableDef.CMS_HOME_TAB;
import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsBannerTableDef.CMS_BANNER;
import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsZoneProductTableDef.CMS_ZONE_PRODUCT;
import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsSpuTableDef.PMS_SPU;
import com.clmcat.qianyu.mall.cms.service.CmsViewBiz;

@Slf4j
@Service
public class CmsViewBizImpl implements CmsViewBiz {

    @Resource
    private CmsHomeTabMapper homeTabMapper;
    @Resource
    private CmsBannerMapper bannerMapper;
    @Resource
    private CmsZoneServiceBiz zoneServiceBiz;
    @Resource
    private CmsZoneProductMapper zoneProductMapper;
    @Resource
    private PmsSpuMapper spuMapper;
    @Resource
    private PmsSupport pmsSupport;

    // ==================== 内存缓存（Tab + Banner）====================

    /** 缓存的 Tab VO 列表（不可变，定时刷新时整体替换） */
    private volatile List<HomeTabVo> cachedTabList = new ArrayList<>();

    /** 缓存的默认 Tab Key */
    private volatile String cachedDefaultTabKey = "recommend";

    /** 缓存的 Banner VO 列表（不可变，定时刷新时整体替换） */
    private volatile List<BannerVo> cachedBannerList = new ArrayList<>();

    /**
     * 每分钟的第 0 秒和第 30 秒刷新 Tab + Banner 缓存
     */
    @Scheduled(cron = "0,30 * * * * *")
    public void refreshTabAndBannerCache() {
        try {
            // 1. 刷新 Tab
            List<CmsHomeTab> tabs = homeTabMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where(CMS_HOME_TAB.STATUS.eq(0))
                            .orderBy(CMS_HOME_TAB.SORT.asc())
            );
            tabs = tabs != null ? tabs : Collections.emptyList();
            String defaultTabKey = "recommend";
            List<HomeTabVo> tabVos = new ArrayList<>();
            for (CmsHomeTab tab : tabs) {
                if (tab.getIsDefault() != null && tab.getIsDefault() == 1) {
                    defaultTabKey = tab.getTabKey();
                }
                tabVos.add(HomeTabVo.builder()
                        .id(tab.getId())
                        .name(tab.getName())
                        .tabKey(tab.getTabKey())
                        .categoryId(tab.getCategoryId())
                        .isDefault(tab.getIsDefault() != null && tab.getIsDefault() == 1)
                        .build());
            }
            this.cachedTabList = tabVos;
            this.cachedDefaultTabKey = defaultTabKey;

            // 2. 刷新 Banner
            List<CmsBanner> banners = bannerMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where(CMS_BANNER.STATUS.eq(0))
                            .orderBy(CMS_BANNER.SORT.asc())
            );
            banners = banners != null ? banners : Collections.emptyList();
            List<BannerVo> bannerVos = new ArrayList<>();
            for (CmsBanner b : banners) {
                bannerVos.add(BannerVo.builder()
                        .id(b.getId())
                        .title(b.getTitle())
                        .desc(b.getDescription())
                        .actionText(b.getActionText())
                        .tagText(b.getTagText())
                        .image(b.getImage())
                        .linkType(b.getLinkType())
                        .linkValue(b.getLinkValue())
                        .build());
            }
            this.cachedBannerList = bannerVos;

            log.debug("Tab+Banner 缓存刷新完成: tabs={}, banners={}", tabVos.size(), bannerVos.size());
        } catch (Exception e) {
            log.error("Tab+Banner 缓存刷新失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 首页聚合数据（一次性返回 Tab + Banner + Zone）
     */
    public HomePageVo getHomePage() {
        // 1. Tab 列表（从内存缓存读取）
        List<HomeTabVo> tabVos = this.cachedTabList;
        String defaultTabKey = this.cachedDefaultTabKey;
        if (tabVos.isEmpty()) {
            refreshTabAndBannerCache();
            tabVos = this.cachedTabList;
            defaultTabKey = this.cachedDefaultTabKey;
        }

        // 2. Banner 列表（从内存缓存读取）
        List<BannerVo> bannerVos = this.cachedBannerList;
        if (bannerVos.isEmpty() && tabVos.isEmpty()) {
            // 如果 tabVos 也为空，上面已经触发过刷新，这里取最新值
            bannerVos = this.cachedBannerList;
        }

        // 3. Zone 列表（带商品，实时查询）
        List<ZoneVo> zoneVos = buildZonesVo(null);

        return HomePageVo.builder()
                .tabList(tabVos)
                .defaultTabKey(defaultTabKey)
                .bannerList(bannerVos)
                .zoneList(zoneVos)
                .build();
    }

    /**
     * 单个 Tab 下的 Zone 列表
     *
     * <p>{@code categoryId} 为 0 / null 时返回 recommend 默认的 zoneList（与 homePage
     * 一致）；非 0 时按该分类过滤 zone 内的商品。
     *
     * <p>TODO: 后续若 CMS 支持"分类专属运营位"，再按 {@code tabZoneConfig}
     * 查该分类绑定的 zone 列表，而不是返回全量 zone。
     */
    @Override
    public TabZoneListVo getTabZoneList(Long categoryId) {
        List<ZoneVo> zoneVos = buildZonesVo(categoryId);
        return TabZoneListVo.builder().zoneList(zoneVos).build();
    }

    /**
     * 构造 ZoneVo 列表（带商品），统一 homePage 和 tabZoneList 的查询逻辑
     *
     * @param tabCategoryId 当前 Tab 关联的分类 ID；null/0 表示"recommend 默认"，
     *                       此时退回原 homePage 行为（按 zone 自身的 categoryId 过滤商品）
     */
    private List<ZoneVo> buildZonesVo(Long tabCategoryId) {
        List<CmsZone> zones = zoneServiceBiz.selectAllEnabled();
        List<ZoneVo> zoneVos = new ArrayList<>();
        for (CmsZone z : zones) {
            List<SpuSimpleVo> products = loadZoneProducts(z, tabCategoryId);
            zoneVos.add(ZoneVo.builder()
                    .id(z.getId())
                    .title(z.getTitle())
                    .tagText(z.getTagText())
                    .moreText(z.getMoreText())
                    .layoutMode(z.getLayoutMode())
                    .surfaceBackground(z.getSurfaceBackground())
                    .surfaceShadow(z.getSurfaceShadow())
                    .productList(products)
                    .build());
        }
        return zoneVos;
    }

    /**
     * 加载 Zone 关联的商品。填充优先级：手动选品 → 自动投放行 → 旧「分类+销量」兜底。
     *
     * <p>fill_mode：0=仅手动（只取手动行，无兜底）/ 1=仅自动（只取自动行+兜底）/ 2=手动优先+自动补足（默认，手动+自动行+兜底）。
     * <p>未配置任何选品行时（默认 MIXED），退化为旧逻辑（按 tabCategoryId 或 zone.categoryId + 销量 TopN），现网零破坏。
     * <p>下架/删除的 SPU 不展示（按 status=1 + deleted=0 过滤）。
     *
     * @param zone          当前 zone
     * @param tabCategoryId 当前 Tab 的分类 ID；null/0 时用 zone 自身的 categoryId（仅兜底用）
     */
    private List<SpuSimpleVo> loadZoneProducts(CmsZone zone, Long tabCategoryId) {
        int cap = zone.getProductCount() != null ? zone.getProductCount() : 4;
        int fillMode = zone.getFillMode() != null ? zone.getFillMode() : CmsZone.FILL_MIXED;
        List<SpuSimpleVo> result = new ArrayList<>();
        Set<Long> picked = new HashSet<>();

        // 1) 选品/投放行（cms_zone_product）：手动(source=0)优先于自动(source=1)
        List<CmsZoneProduct> rows = zoneProductMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(CMS_ZONE_PRODUCT.ZONE_ID.eq(zone.getId()))
                        .and(CMS_ZONE_PRODUCT.STATUS.eq(CmsZoneProduct.STATUS_SHOW))
                        .orderBy(CMS_ZONE_PRODUCT.SOURCE.asc(), CMS_ZONE_PRODUCT.SORT.asc(), CMS_ZONE_PRODUCT.ID.asc()));
        if (rows != null && !rows.isEmpty()) {
            List<Long> spuIds = rows.stream().map(CmsZoneProduct::getSpuId).distinct().collect(Collectors.toList());
            List<PmsSpu> onSale = spuMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where(PMS_SPU.ID.in(spuIds))
                            .and(PMS_SPU.STATUS.eq(1))
                            .and(PMS_SPU.DELETED.eq(0)));
            Map<Long, PmsSpu> onSaleMap = new HashMap<>();
            Set<Long> onSaleIds = new HashSet<>();
            for (PmsSpu s : onSale) {
                onSaleMap.put(s.getId(), s);
                onSaleIds.add(s.getId());
            }
            for (CmsZoneProduct r : rows) {
                if (result.size() >= cap) {
                    break;
                }
                if (fillMode == CmsZone.FILL_MANUAL_ONLY && r.getSource() != CmsZoneProduct.SOURCE_MANUAL) {
                    continue;
                }
                if (fillMode == CmsZone.FILL_AUTO_ONLY && r.getSource() != CmsZoneProduct.SOURCE_AUTO) {
                    continue;
                }
                if (!onSaleIds.contains(r.getSpuId()) || !picked.add(r.getSpuId())) {
                    continue;
                }
                result.add(pmsSupport.toSpuSimpleVo(onSaleMap.get(r.getSpuId())));
            }
        }

        // 2) 不足时旧兜底（仅 AUTO_ONLY/MIXED）：按 tabCategoryId 或 zone.categoryId + 销量补齐
        if (result.size() < cap && fillMode != CmsZone.FILL_MANUAL_ONLY) {
            int need = cap - result.size();
            QueryWrapper qw = QueryWrapper.create()
                    .where(PMS_SPU.STATUS.eq(1))
                    .and(PMS_SPU.DELETED.eq(0));
            if (!picked.isEmpty()) {
                qw.and(PMS_SPU.ID.notIn(picked));
            }
            if (tabCategoryId != null && tabCategoryId > 0) {
                qw.and(PMS_SPU.CATEGORY_ID.eq(tabCategoryId));
            } else if (zone.getCategoryId() != null && zone.getCategoryId() > 0) {
                qw.and(PMS_SPU.CATEGORY_ID.eq(zone.getCategoryId()));
            }
            qw.orderBy(PMS_SPU.SALES.desc()).limit(need);
            List<PmsSpu> legacy = spuMapper.selectListByQuery(qw);
            if (legacy != null) {
                for (PmsSpu s : legacy) {
                    if (result.size() >= cap) {
                        break;
                    }
                    result.add(pmsSupport.toSpuSimpleVo(s));
                }
            }
        }
        return result;
    }
}
