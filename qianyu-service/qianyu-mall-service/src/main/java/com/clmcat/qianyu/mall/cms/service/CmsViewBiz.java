package com.clmcat.qianyu.mall.cms.service;

import com.clmcat.qianyu.mall.cms.mapper.CmsBannerMapper;
import com.clmcat.qianyu.mall.cms.mapper.CmsHomeTabMapper;
import com.clmcat.qianyu.mall.cms.mapper.CmsZoneMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsBanner;
import com.clmcat.qianyu.mall.cms.model.entity.CmsHomeTab;
import com.clmcat.qianyu.mall.cms.model.entity.CmsZone;
import com.clmcat.qianyu.mall.cms.model.vo.BannerVo;
import com.clmcat.qianyu.mall.cms.model.vo.HomePageVo;
import com.clmcat.qianyu.mall.cms.model.vo.HomeTabVo;
import com.clmcat.qianyu.mall.cms.model.vo.ZoneVo;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsHomeTabTableDef.CMS_HOME_TAB;
import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsBannerTableDef.CMS_BANNER;
import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsZoneTableDef.CMS_ZONE;
import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsSpuTableDef.PMS_SPU;

@Service
public class CmsViewBiz {

    @Resource
    private CmsHomeTabMapper homeTabMapper;
    @Resource
    private CmsBannerMapper bannerMapper;
    @Resource
    private CmsZoneMapper zoneMapper;
    @Resource
    private PmsSpuMapper spuMapper;
    @Resource
    private PmsSupport pmsSupport;

    /**
     * 首页聚合数据（一次性返回 Tab + Banner + Zone）
     */
    public HomePageVo getHomePage() {
        // 1. Tab 列表
        List<CmsHomeTab> tabs = homeTabMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(CMS_HOME_TAB.STATUS.eq(0))
                        .orderBy(CMS_HOME_TAB.SORT.asc())
        );
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

        // 2. Banner 列表
        List<CmsBanner> banners = bannerMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(CMS_BANNER.STATUS.eq(0))
                        .orderBy(CMS_BANNER.SORT.asc())
        );
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

        // 3. Zone 列表（带商品）
        List<CmsZone> zones = zoneMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(CMS_ZONE.STATUS.eq(0))
                        .orderBy(CMS_ZONE.SORT.asc())
        );
        List<ZoneVo> zoneVos = new ArrayList<>();
        for (CmsZone z : zones) {
            List<SpuSimpleVo> products = loadZoneProducts(z);
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

        return HomePageVo.builder()
                .tabList(tabVos)
                .defaultTabKey(defaultTabKey)
                .bannerList(bannerVos)
                .zoneList(zoneVos)
                .build();
    }

    /**
     * 加载 Zone 关联的商品（按分类或全部，取销量前 N）
     */
    private List<SpuSimpleVo> loadZoneProducts(CmsZone zone) {
        int count = zone.getProductCount() != null ? zone.getProductCount() : 4;

        QueryWrapper qw = QueryWrapper.create()
                .where(PMS_SPU.STATUS.eq(1));

        if (zone.getCategoryId() != null) {
            qw.and(PMS_SPU.CATEGORY_ID.eq(zone.getCategoryId()));
        }

        qw.orderBy(PMS_SPU.SALES.desc());
        qw.limit(count);

        List<PmsSpu> spuList = spuMapper.selectListByQuery(qw);

        List<SpuSimpleVo> result = new ArrayList<>();
        for (PmsSpu spu : spuList) {
            result.add(pmsSupport.toSpuSimpleVo(spu));
        }
        return result;
    }
}
