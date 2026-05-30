package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.MerchantStoreApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantStoreDto;
import com.clmcat.qianyu.mall.pms.model.dto.SkuBatchUpdateDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuUpdateDto;
import com.clmcat.qianyu.mall.pms.model.entity.PmsBrand;
import com.clmcat.qianyu.mall.pms.model.entity.PmsCategory;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSku;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpuCategory;
import com.clmcat.qianyu.mall.pms.model.entity.status.PmsStatus;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PmsMerchantViewBiz {

    @Resource
    private PmsSpuServiceBiz spuServiceBiz;

    @Resource
    private PmsSkuServiceBiz skuServiceBiz;

    @Resource
    private PmsCategoryServiceBiz categoryServiceBiz;

    @Resource
    private PmsBrandServiceBiz brandServiceBiz;

    @DubboReference
    private MerchantApi merchantApi;

    @DubboReference
    private MerchantStoreApi merchantStoreApi;

    @DubboReference
    private InvStockApi invStockApi;

    @Resource
    private PmsSupport pmsSupport;

    /**
     * 创建 SPU
     */
    @Transactional
    public Long createSpu(long userId, SpuCreateDto dto) {
        // 参数校验
        PmsStatus.PMS_SPU_PARAM_INVALID.assertThrowResEx(
                dto.getName() == null || dto.getName().isEmpty() || dto.getName().length() > 200);
        PmsStatus.PMS_SPU_PARAM_INVALID.assertThrowResEx(
                dto.getImages() == null || dto.getImages().isEmpty());
        PmsStatus.PMS_SPU_PARAM_INVALID.assertThrowResEx(
                dto.getSkuList() == null || dto.getSkuList().isEmpty());

        // 校验 SKU 价格
        for (SpuCreateDto.SkuCreateItem skuItem : dto.getSkuList()) {
            if (skuItem.getPrice() != null) {
                BigDecimal price = new BigDecimal(skuItem.getPrice());
                PmsStatus.PMS_SKU_PRICE_INVALID.assertThrowResEx(price.compareTo(BigDecimal.ZERO) <= 0);
            }
        }

        // 校验分类存在
        if (dto.getCategoryId() != null) {
            PmsCategory category = categoryServiceBiz.selectOneById(dto.getCategoryId());
            PmsStatus.PMS_CATEGORY_NOT_FOUND.assertThrowResEx(category == null);
        }

        // 校验品牌存在
        if (dto.getBrandId() != null) {
            PmsBrand brand = brandServiceBiz.selectOneById(dto.getBrandId());
            PmsStatus.PMS_BRAND_NOT_FOUND.assertThrowResEx(brand == null);
        }

        // Resolve merchantId via MCH module
        MerchantDto merchantDto = merchantApi.getByUserId(userId);
        Long merchantId = merchantDto != null ? merchantDto.getId() : userId;
        Long storeId = 0L;
        if (merchantDto != null) {
            MerchantStoreDto storeDto = merchantStoreApi.getByMerchantId(merchantDto.getId());
            if (storeDto != null) {
                storeId = storeDto.getId();
            }
        }

        // 生成 SPU ID
        long spuId = pmsSupport.nextId();
        long now = pmsSupport.parseTime(spuId);

        // 计算 minPrice
        BigDecimal minPrice = null;
        for (SpuCreateDto.SkuCreateItem skuItem : dto.getSkuList()) {
            if (skuItem.getPrice() != null) {
                BigDecimal p = new BigDecimal(skuItem.getPrice());
                if (minPrice == null || p.compareTo(minPrice) < 0) {
                    minPrice = p;
                }
            }
        }

        // 插入 SPU（status=0 草稿）
        PmsSpu spu = new PmsSpu();
        spu.setId(spuId);
        spu.setMerchantId(merchantId);
        spu.setStoreId(storeId);
        spu.setBrandId(dto.getBrandId());
        spu.setCategoryId(dto.getCategoryId());
        spu.setName(dto.getName());
        spu.setSubtitle(dto.getSubTitle());
        spu.setMainImage(dto.getMainImage());
        spu.setThumbImage(dto.getMainImage()); // 直接使用 mainImage 作为 thumbImage
        spu.setImages(dto.getImages());
        spu.setDescription(dto.getDescription());
        spu.setKeywords(dto.getKeywords());
        spu.setUnit(dto.getUnit() != null ? dto.getUnit() : "个");
        spu.setStatus(0); // 草稿
        spu.setSort(0);
        spu.setFreightTemplateId(dto.getFreightTemplateId());
        spu.setMinPrice(minPrice);
        spu.setSales(0);
        spu.setCommentCount(0);
        spu.setAvgScore(BigDecimal.ZERO);
        spu.setCreateTime(now);
        spu.setUpdateTime(now);
        spu.setDeleted(0);
        spuServiceBiz.insertSelective(spu);

        // 处理 SKU 列表
        boolean hasDefault = false;
        for (SpuCreateDto.SkuCreateItem skuItem : dto.getSkuList()) {
            long skuId = pmsSupport.nextId();
            long skuNow = pmsSupport.parseTime(skuId);

            boolean isDefault = skuItem.getIsDefault() != null && skuItem.getIsDefault();
            if (!hasDefault && (isDefault || skuItem == dto.getSkuList().get(0))) {
                if (!isDefault && !hasDefault) {
                    isDefault = true;
                }
            }

            PmsSku sku = new PmsSku();
            sku.setId(skuId);
            sku.setMerchantId(merchantId);
            sku.setSpuId(spuId);
            sku.setSkuCode(String.valueOf(skuId));
            sku.setSkuName(skuItem.getSkuName());
            // specs -> attributes
            if (skuItem.getSpecs() != null && dto.getCategoryId() != null) {
                sku.setAttributes(pmsSupport.specsToAttributes(skuItem.getSpecs(), dto.getCategoryId()));
            }
            sku.setSkuImage(skuItem.getImage());
            sku.setPrice(skuItem.getPrice() != null ? new BigDecimal(skuItem.getPrice()) : null);
            sku.setOriginalPrice(skuItem.getOriginalPrice() != null ? new BigDecimal(skuItem.getOriginalPrice()) : null);
            sku.setCostPrice(null);
            sku.setStatus(0); // 上架
            sku.setIsDefault(isDefault ? 1 : 0);
            sku.setWeight(skuItem.getWeight());
            sku.setVolume(skuItem.getVolume());
            sku.setCreateTime(skuNow);
            sku.setUpdateTime(skuNow);
            sku.setDeleted(0);
            skuServiceBiz.insertSelective(sku);

            if (isDefault) {
                hasDefault = true;
            }

            // Initialize stock via INV module API
            try {
                if (skuItem.getStock() != null && skuItem.getStock() > 0) {
                    invStockApi.adjustStock(sku.getId(), 1, skuItem.getStock(), "新建商品初始化库存");
                }
            } catch (Exception e) {
                log.warn("库存初始化失败, spuId={}, error={}", spuId, e.getMessage());
            }
        }

        // 插入 SPU-分类关联
        if (dto.getCategoryId() != null) {
            long relId = pmsSupport.nextId();
            PmsSpuCategory rel = new PmsSpuCategory();
            rel.setId(relId);
            rel.setSpuId(spuId);
            rel.setCategoryId(dto.getCategoryId());
            rel.setCreateTime(pmsSupport.parseTime(relId));
            List<PmsSpuCategory> relList = new ArrayList<>();
            relList.add(rel);
            spuServiceBiz.batchInsertSpuCategories(relList);
        }

        return spuId;
    }

    /**
     * 编辑 SPU
     */
    @Transactional
    public void updateSpu(long userId, SpuUpdateDto dto) {
        PmsSpu spu = spuServiceBiz.selectOneById(dto.getSpuId());
        PmsStatus.PMS_SPU_NOT_FOUND.assertThrowResEx(spu == null || spu.getDeleted() == 1);
        Long merchantId = resolveMerchantId(userId);
        PmsStatus.PMS_SPU_NOT_OWNER.assertThrowResEx(
                spu.getMerchantId() == null || !spu.getMerchantId().equals(merchantId));

        long now = System.currentTimeMillis();

        // 若修改 categoryId，校验新分类存在
        if (dto.getCategoryId() != null) {
            PmsCategory category = categoryServiceBiz.selectOneById(dto.getCategoryId());
            PmsStatus.PMS_CATEGORY_NOT_FOUND.assertThrowResEx(category == null);
        }

        // 若修改 brandId，校验新品牌存在
        if (dto.getBrandId() != null) {
            PmsBrand brand = brandServiceBiz.selectOneById(dto.getBrandId());
            PmsStatus.PMS_BRAND_NOT_FOUND.assertThrowResEx(brand == null);
        }

        // 若修改 skuList（全量替换）
        if (dto.getSkuList() != null && !dto.getSkuList().isEmpty()) {
            // 逻辑删除旧 SKU
            List<PmsSku> oldSkuList = skuServiceBiz.selectBySpuId(dto.getSpuId());
            for (PmsSku oldSku : oldSkuList) {
                PmsSku deleteSku = new PmsSku();
                deleteSku.setId(oldSku.getId());
                deleteSku.setDeleted(1);
                deleteSku.setUpdateTime(now);
                skuServiceBiz.updateSku(deleteSku);
            }

            // 插入新 SKU
            Long spuMerchantId = spu.getMerchantId();
            boolean hasDefault = false;
            for (SpuCreateDto.SkuCreateItem skuItem : dto.getSkuList()) {
                long skuId = pmsSupport.nextId();
                long skuNow = pmsSupport.parseTime(skuId);

                boolean isDefault = skuItem.getIsDefault() != null && skuItem.getIsDefault();
                if (!hasDefault && !isDefault) {
                    isDefault = true;
                }

                PmsSku sku = new PmsSku();
                sku.setId(skuId);
                sku.setMerchantId(spuMerchantId);
                sku.setSpuId(dto.getSpuId());
                sku.setSkuCode(String.valueOf(skuId));
                sku.setSkuName(skuItem.getSkuName());
                if (skuItem.getSpecs() != null && dto.getCategoryId() != null) {
                    sku.setAttributes(pmsSupport.specsToAttributes(skuItem.getSpecs(), dto.getCategoryId()));
                } else if (skuItem.getSpecs() != null && spu.getCategoryId() != null) {
                    sku.setAttributes(pmsSupport.specsToAttributes(skuItem.getSpecs(), spu.getCategoryId()));
                }
                sku.setSkuImage(skuItem.getImage());
                sku.setPrice(skuItem.getPrice() != null ? new BigDecimal(skuItem.getPrice()) : null);
                sku.setOriginalPrice(skuItem.getOriginalPrice() != null ? new BigDecimal(skuItem.getOriginalPrice()) : null);
                sku.setStatus(0);
                sku.setIsDefault(isDefault ? 1 : 0);
                sku.setWeight(skuItem.getWeight());
                sku.setVolume(skuItem.getVolume());
                sku.setCreateTime(skuNow);
                sku.setUpdateTime(skuNow);
                sku.setDeleted(0);
                skuServiceBiz.insertSelective(sku);

                if (isDefault) {
                    hasDefault = true;
                }
            }

            // 重新计算 minPrice
            pmsSupport.refreshMinPrice(dto.getSpuId());
        }

        // 更新 SPU 非空字段
        PmsSpu update = new PmsSpu();
        update.setId(dto.getSpuId());
        if (dto.getName() != null) {
            update.setName(dto.getName());
        }
        if (dto.getSubTitle() != null) {
            update.setSubtitle(dto.getSubTitle());
        }
        if (dto.getCategoryId() != null) {
            update.setCategoryId(dto.getCategoryId());
        }
        if (dto.getBrandId() != null) {
            update.setBrandId(dto.getBrandId());
        }
        if (dto.getMainImage() != null) {
            update.setMainImage(dto.getMainImage());
            update.setThumbImage(dto.getMainImage());
        }
        if (dto.getImages() != null) {
            update.setImages(dto.getImages());
        }
        if (dto.getDescription() != null) {
            update.setDescription(dto.getDescription());
        }
        if (dto.getKeywords() != null) {
            update.setKeywords(dto.getKeywords());
        }
        if (dto.getUnit() != null) {
            update.setUnit(dto.getUnit());
        }
        if (dto.getFreightTemplateId() != null) {
            update.setFreightTemplateId(dto.getFreightTemplateId());
        }
        update.setUpdateTime(now);
        spuServiceBiz.updateSpu(update);

        // 更新 SPU-分类关联（若 categoryId 变更）
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(spu.getCategoryId())) {
            spuServiceBiz.deleteSpuCategoryBySpuId(dto.getSpuId());
            long relId = pmsSupport.nextId();
            PmsSpuCategory rel = new PmsSpuCategory();
            rel.setId(relId);
            rel.setSpuId(dto.getSpuId());
            rel.setCategoryId(dto.getCategoryId());
            rel.setCreateTime(pmsSupport.parseTime(relId));
            List<PmsSpuCategory> relList = new ArrayList<>();
            relList.add(rel);
            spuServiceBiz.batchInsertSpuCategories(relList);
        }
    }

    /**
     * SPU 上架
     */
    @Transactional
    public void listOnSpu(long userId, Long spuId) {
        PmsSpu spu = spuServiceBiz.selectOneById(spuId);
        PmsStatus.PMS_SPU_NOT_FOUND.assertThrowResEx(spu == null || spu.getDeleted() == 1);
        Long merchantId = resolveMerchantId(userId);
        PmsStatus.PMS_SPU_NOT_OWNER.assertThrowResEx(
                spu.getMerchantId() == null || !spu.getMerchantId().equals(merchantId));

        long now = System.currentTimeMillis();

        // 更新 SPU status=1
        PmsSpu update = new PmsSpu();
        update.setId(spuId);
        update.setStatus(1);
        update.setPublishTime(now);
        update.setUpdateTime(now);
        spuServiceBiz.updateSpu(update);

        // 同步更新所有 SKU status=0（上架）
        List<PmsSku> skuList = skuServiceBiz.selectBySpuId(spuId);
        for (PmsSku sku : skuList) {
            PmsSku skuUpdate = new PmsSku();
            skuUpdate.setId(sku.getId());
            skuUpdate.setStatus(0);
            skuUpdate.setUpdateTime(now);
            skuServiceBiz.updateSku(skuUpdate);
        }
    }

    /**
     * SPU 下架
     */
    @Transactional
    public void listOffSpu(long userId, Long spuId) {
        PmsSpu spu = spuServiceBiz.selectOneById(spuId);
        PmsStatus.PMS_SPU_NOT_FOUND.assertThrowResEx(spu == null || spu.getDeleted() == 1);
        Long merchantId = resolveMerchantId(userId);
        PmsStatus.PMS_SPU_NOT_OWNER.assertThrowResEx(
                spu.getMerchantId() == null || !spu.getMerchantId().equals(merchantId));

        long now = System.currentTimeMillis();

        // 更新 SPU status=2
        PmsSpu update = new PmsSpu();
        update.setId(spuId);
        update.setStatus(2);
        update.setUpdateTime(now);
        spuServiceBiz.updateSpu(update);

        // 同步更新所有 SKU status=1（下架）
        List<PmsSku> skuList = skuServiceBiz.selectBySpuId(spuId);
        for (PmsSku sku : skuList) {
            PmsSku skuUpdate = new PmsSku();
            skuUpdate.setId(sku.getId());
            skuUpdate.setStatus(1);
            skuUpdate.setUpdateTime(now);
            skuServiceBiz.updateSku(skuUpdate);
        }
    }

    /**
     * SKU 批量更新
     */
    @Transactional
    public void skuBatchUpdate(long userId, SkuBatchUpdateDto dto) {
        PmsStatus.PMS_SPU_PARAM_INVALID.assertThrowResEx(dto == null || dto.getSpuId() == null);
        PmsStatus.PMS_SPU_PARAM_INVALID.assertThrowResEx(dto.getItems() == null || dto.getItems().isEmpty());
        PmsSpu spu = spuServiceBiz.selectOneById(dto.getSpuId());
        PmsStatus.PMS_SPU_NOT_FOUND.assertThrowResEx(spu == null || spu.getDeleted() == 1);
        Long merchantId = resolveMerchantId(userId);
        PmsStatus.PMS_SPU_NOT_OWNER.assertThrowResEx(
                spu.getMerchantId() == null || !spu.getMerchantId().equals(merchantId));

        long now = System.currentTimeMillis();

        for (SkuBatchUpdateDto.SkuUpdateItem item : dto.getItems()) {
            PmsSku sku = skuServiceBiz.selectOneById(item.getSkuId());
            PmsStatus.PMS_SKU_NOT_FOUND.assertThrowResEx(sku == null || sku.getDeleted() == 1);

            // 若设为默认 SKU，先清除旧默认
            if (item.getIsDefault() != null && item.getIsDefault()) {
                skuServiceBiz.clearDefault(dto.getSpuId(), now);
            }

            PmsSku update = new PmsSku();
            update.setId(item.getSkuId());
            if (item.getSkuName() != null) {
                update.setSkuName(item.getSkuName());
            }
            if (item.getPrice() != null) {
                BigDecimal price = new BigDecimal(item.getPrice());
                PmsStatus.PMS_SKU_PRICE_INVALID.assertThrowResEx(price.compareTo(BigDecimal.ZERO) <= 0);
                update.setPrice(price);
            }
            if (item.getOriginalPrice() != null) {
                update.setOriginalPrice(new BigDecimal(item.getOriginalPrice()));
            }
            if (item.getImage() != null) {
                update.setSkuImage(item.getImage());
            }
            if (item.getIsDefault() != null && item.getIsDefault()) {
                update.setIsDefault(1);
            }
            if (item.getWeight() != null) {
                update.setWeight(item.getWeight());
            }
            if (item.getVolume() != null) {
                update.setVolume(item.getVolume());
            }
            update.setUpdateTime(now);
            skuServiceBiz.updateSku(update);

            // TODO：替换真实接口 — 若 stock 更新，调用 inv RPC 调整库存
        }

        // 重新计算 minPrice
        pmsSupport.refreshMinPrice(dto.getSpuId());
    }

    private Long resolveMerchantId(long userId) {
        MerchantDto merchantDto = merchantApi.getByUserId(userId);
        return merchantDto != null ? merchantDto.getId() : userId;
    }
}
