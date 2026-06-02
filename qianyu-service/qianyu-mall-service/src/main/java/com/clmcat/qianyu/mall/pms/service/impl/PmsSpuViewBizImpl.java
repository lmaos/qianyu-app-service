package com.clmcat.qianyu.mall.pms.service.impl;

import com.clmcat.qianyu.mall.pms.rpc.PmsBrandApiImpl;
import com.clmcat.qianyu.mall.pms.rpc.PmsCategoryApiImpl;
import com.clmcat.qianyu.mall.pms.rpc.PmsSkuApiImpl;
import com.clmcat.qianyu.mall.pms.rpc.PmsSpuApiImpl;
import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;
import com.clmcat.qianyu.mall.api.mch.MerchantStoreApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantStoreDto;
import com.clmcat.qianyu.mall.mch.mapper.MerchantMapper;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import com.clmcat.qianyu.mall.pms.model.dto.SpuSearchDto;
import com.clmcat.qianyu.mall.pms.model.entity.PmsBrand;
import com.clmcat.qianyu.mall.pms.model.entity.PmsCategory;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSku;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.entity.status.PmsStatus;
import com.clmcat.qianyu.mall.pms.model.vo.ReviewStatVo;
import com.clmcat.qianyu.mall.pms.model.vo.SkuItemVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpecGroupVo;
import com.clmcat.qianyu.mall.pms.model.vo.ShopSimpleVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuDetailVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.clmcat.qianyu.mall.rev.model.vo.ReviewStatVO;
import com.clmcat.qianyu.mall.rev.service.RevReviewStatViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.clmcat.qianyu.mall.pms.service.PmsSpuViewBiz;

@Service
public class PmsSpuViewBizImpl implements PmsSpuViewBiz {

    @Resource
    private PmsSpuApiImpl spuServiceBiz;

    @Resource
    private PmsSkuApiImpl skuServiceBiz;

    @Resource
    private PmsCategoryApiImpl categoryServiceBiz;

    @Resource
    private PmsBrandApiImpl brandServiceBiz;

    @Resource
    private PmsSupport pmsSupport;

    @Resource
    private RevReviewStatViewServiceBiz reviewStatViewServiceBiz;

    @Resource
    private MerchantMapper merchantMapper;

    @DubboReference
    private InvStockApi invStockApi;

    @DubboReference
    private MerchantStoreApi merchantStoreApi;

    /**
     * SPU 搜索
     */
    public Page<SpuSimpleVo> searchSpu(SpuSearchDto dto) {
        if (dto == null) {
            dto = new SpuSearchDto();
        }

        String sortField = dto.getSortField() != null ? dto.getSortField() : "createTime";
        String sortOrder = dto.getSortOrder() != null ? dto.getSortOrder().toLowerCase() : "desc";
        if (!"asc".equals(sortOrder) && !"desc".equals(sortOrder)) {
            sortOrder = "desc";
        }
        int pageNum = dto.getPageNum() != null ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 10;

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        if (dto.getMinPrice() != null && !dto.getMinPrice().isEmpty()) {
            minPrice = new BigDecimal(dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null && !dto.getMaxPrice().isEmpty()) {
            maxPrice = new BigDecimal(dto.getMaxPrice());
        }

        QueryWrapper qw = QueryWrapper.create()
                .where("status = 1").and("deleted = 0");
        if (dto.getKeyword() != null && !dto.getKeyword().isEmpty()) {
            String likePattern = "%" + dto.getKeyword() + "%";
            qw.and("(name LIKE ? OR keywords LIKE ?)", likePattern, likePattern);
        }
        if (dto.getCategoryId() != null) {
            qw.and("category_id = ?", dto.getCategoryId());
        }
        if (dto.getBrandId() != null) {
            qw.and("brand_id = ?", dto.getBrandId());
        }
        if (minPrice != null) {
            qw.and("min_price >= ?", minPrice);
        }
        if (maxPrice != null) {
            qw.and("min_price <= ?", maxPrice);
        }

        String orderColumn;
        if ("price".equals(sortField)) {
            orderColumn = "min_price";
        } else if ("sales".equals(sortField)) {
            orderColumn = "sales";
        } else {
            orderColumn = "create_time";
        }
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);
        qw.orderBy(orderColumn, !ascending);

        Page<PmsSpu> spuPage = spuServiceBiz.paginate(new Page<>(pageNum, pageSize), qw);
        if (spuPage == null || spuPage.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        return spuPage.map(pmsSupport::toSpuSimpleVo);
    }

    /**
     * SPU 详情
     */
    public SpuDetailVo getSpuDetail(Long spuId) {
        PmsStatus.PMS_SPU_NOT_FOUND.assertThrowResEx(spuId == null);
        PmsSpu spu = spuServiceBiz.selectOneById(spuId);
        PmsStatus.PMS_SPU_NOT_FOUND.assertThrowResEx(spu == null || spu.getDeleted() == 1);
        PmsStatus.PMS_SPU_OFF_SHELF.assertThrowResEx(spu.getStatus() != 1);

        // 查询 SKU 列表
        List<PmsSku> skuList = skuServiceBiz.selectBySpuId(spuId);

        // Batch query stock via INV module API
        java.util.Map<Long, Integer> stockMap = new java.util.HashMap<>();
        try {
            java.util.List<Long> skuIds = new ArrayList<>();
            for (PmsSku sku : skuList) { skuIds.add(sku.getId()); }
            if (!skuIds.isEmpty()) {
                List<InvStockDto> stockInfos = invStockApi.batchQuery(skuIds);
                for (InvStockDto si : stockInfos) {
                    stockMap.put(si.getSkuId(), si.getAvailableStock());
                }
            }
        } catch (Exception e) {
            // Stock query failure should not block SPU detail
        }

        // 转换为 SkuItemVo
        List<SkuItemVo> skuItemVos = new ArrayList<>();
        Long defaultSkuId = null;
        for (PmsSku sku : skuList) {
            if (sku.getIsDefault() != null && sku.getIsDefault() == 1 && defaultSkuId == null) {
                defaultSkuId = sku.getId();
            }
            skuItemVos.add(pmsSupport.toSkuItemVo(sku, spu.getMainImage(), stockMap.getOrDefault(sku.getId(), 0)));
        }
        if (defaultSkuId == null && !skuList.isEmpty()) {
            defaultSkuId = skuList.get(0).getId();
        }

        // 分类名称
        String categoryName = null;
        if (spu.getCategoryId() != null) {
            PmsCategory category = categoryServiceBiz.selectOneById(spu.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        // 品牌名称
        String brandName = null;
        if (spu.getBrandId() != null) {
            PmsBrand brand = brandServiceBiz.selectOneById(spu.getBrandId());
            if (brand != null) {
                brandName = brand.getName();
            }
        }

        // 默认 SKU 原价
        BigDecimal originalPrice = pmsSupport.getDefaultSkuOriginalPrice(spuId);

        // 规格组
        List<SpecGroupVo> specGroups = pmsSupport.buildSpecGroups(skuList);

        // 评价统计（调用 rev 模块获取完整统计）
        ReviewStatVo reviewStat = buildReviewStat(spuId);

        // 商家/店铺信息
        String merchantName = null;
        String storeName = null;
        ShopSimpleVo shopInfo = null;
        if (spu.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectOneById(spu.getMerchantId());
            if (merchant != null) {
                merchantName = merchant.getName();
            }
            try {
                MerchantStoreDto store = merchantStoreApi.getByMerchantId(spu.getMerchantId());
                if (store != null) {
                    storeName = store.getName();
                    shopInfo = ShopSimpleVo.builder()
                            .merchantId(spu.getMerchantId())
                            .storeId(store.getId())
                            .shopName(store.getName() != null ? store.getName() : merchantName)
                            .shopLogo(store.getLogo())
                            .description(store.getDescription())
                            .build();
                }
            } catch (Exception e) {
                // store query failure should not block
            }
            if (shopInfo == null && merchant != null) {
                shopInfo = ShopSimpleVo.builder()
                        .merchantId(spu.getMerchantId())
                        .shopName(merchantName)
                        .build();
            }
        }

        return SpuDetailVo.builder()
                .id(spu.getId())
                .name(spu.getName())
                .subTitle(spu.getSubtitle())
                .mainImage(spu.getMainImage())
                .images(spu.getImages())
                .description(spu.getDescription())
                .categoryId(spu.getCategoryId())
                .categoryName(categoryName)
                .brandId(spu.getBrandId())
                .brandName(brandName)
                .price(pmsSupport.amountToString(spu.getMinPrice()))
                .originalPrice(pmsSupport.amountToString(originalPrice))
                .sales(spu.getSales() != null ? spu.getSales() : 0)
                .commentCount(spu.getCommentCount() != null ? spu.getCommentCount() : 0)
                .avgScore(spu.getAvgScore())
                .unit(spu.getUnit())
                .keywords(spu.getKeywords())
                .freightTemplateId(spu.getFreightTemplateId())
                .merchantId(spu.getMerchantId())
                .merchantName(merchantName)
                .storeId(spu.getStoreId())
                .storeName(storeName)
                .skuList(skuItemVos)
                .specGroups(specGroups)
                .defaultSkuId(defaultSkuId)
                .reviewStat(reviewStat)
                .shopInfo(shopInfo)
                .build();
    }

    /**
     * SPU 详情（带 SKU 预选）
     */
    public SpuDetailVo getSpuDetailBySku(Long skuId) {
        PmsSku sku = skuServiceBiz.selectOneById(skuId);
        PmsStatus.PMS_SKU_NOT_FOUND.assertThrowResEx(sku == null || sku.getDeleted() == 1);
        return getSpuDetail(sku.getSpuId());
    }

    /**
     * 调用 rev 模块获取完整评价统计，映射为 pms 包下的 ReviewStatVo
     * rev.ReviewStatVO.mediumCount → pms.ReviewStatVo.midCount
     * rev.ReviewStatVO.hasImageCount → pms.ReviewStatVo.imageCount
     */
    private ReviewStatVo buildReviewStat(Long spuId) {
        try {
            ReviewStatVO stat = reviewStatViewServiceBiz.getReviewStat(spuId);
            return ReviewStatVo.builder()
                    .totalCount(stat.getTotalCount())
                    .goodCount(stat.getGoodCount())
                    .midCount(stat.getMediumCount())
                    .badCount(stat.getBadCount())
                    .imageCount(stat.getHasImageCount())
                    .avgScore(stat.getAvgScore())
                    .goodRate(stat.getGoodRate())
                    .build();
        } catch (Exception e) {
            // rev 模块异常时不阻断详情页
            return ReviewStatVo.builder()
                    .totalCount(0)
                    .goodCount(0)
                    .midCount(0)
                    .badCount(0)
                    .imageCount(0)
                    .avgScore(BigDecimal.ZERO)
                    .goodRate("0.00%")
                    .build();
        }
    }
}
