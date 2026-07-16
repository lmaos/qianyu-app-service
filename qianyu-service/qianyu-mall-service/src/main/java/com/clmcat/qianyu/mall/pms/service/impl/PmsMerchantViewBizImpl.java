package com.clmcat.qianyu.mall.pms.service.impl;

import com.clmcat.qianyu.mall.pms.rpc.PmsBrandApiImpl;
import com.clmcat.qianyu.mall.pms.rpc.PmsCategoryApiImpl;
import com.clmcat.qianyu.mall.pms.rpc.PmsSkuApiImpl;
import com.clmcat.qianyu.mall.pms.rpc.PmsSpuApiImpl;
import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.MerchantStoreApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantStoreDto;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.dto.MerchantGoodsQueryDTO;
import com.clmcat.qianyu.mall.pms.model.dto.SkuBatchUpdateDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuUpdateDto;
import com.clmcat.qianyu.mall.pms.model.entity.PmsBrand;
import com.clmcat.qianyu.mall.pms.model.entity.PmsCategory;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSku;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpuCategory;
import com.clmcat.qianyu.mall.pms.model.entity.status.PmsStatus;
import com.clmcat.qianyu.mall.pms.model.vo.MerchantGoodsPageVO;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.clmcat.qianyu.mall.pms.service.PmsMerchantViewBiz;
import com.clmcat.qianyu.mall.pms.service.PmsSpuStatusChanger;

@Service
@Slf4j
public class PmsMerchantViewBizImpl implements PmsMerchantViewBiz {

    @Resource
    private PmsSpuApiImpl spuServiceBiz;

    @Resource
    private PmsSpuMapper spuMapper;

    @Resource
    private PmsSkuApiImpl skuServiceBiz;

    @Resource
    private PmsCategoryApiImpl categoryServiceBiz;

    @Resource
    private PmsBrandApiImpl brandServiceBiz;

    @DubboReference
    private MerchantApi merchantApi;

    @DubboReference
    private MerchantStoreApi merchantStoreApi;

    @DubboReference
    private InvStockApi invStockApi;

    @Resource
    private PmsSupport pmsSupport;

    @Resource
    private PmsSpuStatusChanger spuStatusChanger;

    /**
     * 商家商品管理页聚合
     */
    public MerchantGoodsPageVO getGoodsPage(long userId, MerchantGoodsQueryDTO dto) {
        Long merchantId = resolveMerchantId(userId);

        int filter = dto != null && dto.getFilter() != null ? dto.getFilter() : 0;
        int pageNum = dto != null && dto.getPageNum() != null ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null ? dto.getPageSize() : 10;

        // 1. 统计卡片
        long totalCount = spuMapper.selectCountByQuery(
                QueryWrapper.create().where("merchant_id = ?", merchantId).and("deleted = 0"));
        long sellingCount = spuMapper.selectCountByQuery(
                QueryWrapper.create().where("merchant_id = ?", merchantId).and("status = 1").and("deleted = 0"));
        long offCount = spuMapper.selectCountByQuery(
                QueryWrapper.create().where("merchant_id = ?", merchantId).and("status IN (0, 2)").and("deleted = 0"));
        long auditingCount = spuMapper.selectCountByQuery(
                QueryWrapper.create().where("merchant_id = ?", merchantId).and("status IN (4, 5)").and("deleted = 0"));

        List<MerchantGoodsPageVO.SummaryItem> summaryList = new ArrayList<>();
        summaryList.add(MerchantGoodsPageVO.SummaryItem.builder().key("all").label("全部商品").value(String.valueOf(totalCount)).build());
        summaryList.add(MerchantGoodsPageVO.SummaryItem.builder().key("selling").label("在售中").value(String.valueOf(sellingCount)).build());
        summaryList.add(MerchantGoodsPageVO.SummaryItem.builder().key("auditing").label("审核中").value(String.valueOf(auditingCount)).build());
        summaryList.add(MerchantGoodsPageVO.SummaryItem.builder().key("warning").label("库存预警").value("0").build());

        // 2. 商品列表查询
        QueryWrapper qw = QueryWrapper.create()
                .where("merchant_id = ?", merchantId).and("deleted = 0");
        switch (filter) {
            case 1 -> qw.and("status = 1");           // 在售
            case 2 -> qw.and("status IN (0, 2)");     // 待上架(草稿+下架)
            case 4 -> qw.and("status IN (4, 5)");     // 审核中(待审核+审核通过)
            case 3 -> { /* 库存预警 P2, 暂无数据 */ }
            default -> { /* 全部 */ }
        }
        qw.orderBy("create_time DESC");

        Page<PmsSpu> spuPage = spuMapper.paginate(new Page<>(pageNum, pageSize), qw);
        if (spuPage == null) {
            return MerchantGoodsPageVO.builder()
                    .summaryList(summaryList)
                    .goodsList(new Page<>())
                    .build();
        }

        Page<MerchantGoodsPageVO.GoodsItem> goodsPage = spuPage.map(spu -> {
            String statusText = switch (spu.getStatus()) {
                case 1 -> "在售中";
                case 0 -> "待上架";
                case 2 -> "已下架";
                case 4 -> "审核中";
                case 5 -> "可上架";
                default -> "未知";
            };
            String price = spu.getMinPrice() != null
                    ? spu.getMinPrice().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
            String image = spu.getThumbImage() != null ? spu.getThumbImage() : spu.getMainImage();
            return MerchantGoodsPageVO.GoodsItem.builder()
                    .id(spu.getId())
                    .title(spu.getName())
                    .coverBackground(image)
                    .coverText(spu.getName() != null && spu.getName().length() > 0
                            ? spu.getName().substring(0, 1) : "商")
                    .price(price)
                    .stockText("—" + (spu.getSales() != null ? spu.getSales() : 0) + " 销量")
                    .statusText(statusText)
                    .sales(spu.getSales() != null ? spu.getSales() : 0)
                    .build();
        });

        return MerchantGoodsPageVO.builder()
                .summaryList(summaryList)
                .goodsList(goodsPage)
                .build();
    }

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

        // 商户身份门禁：必须已审核通过且生效（收紧"成为商户后才能上架"）；原 fallback→userId 已移除
        MerchantDto merchantDto = merchantApi.requireActiveMerchant(userId);
        Long merchantId = merchantDto.getId();
        Long storeId = 0L;
        MerchantStoreDto storeDto = merchantStoreApi.getByMerchantId(merchantDto.getId());
        if (storeDto != null) {
            storeId = storeDto.getId();
        }

        // 生成 SPU ID
        long spuId = pmsSupport.nextId();
        long now = pmsSupport.parseTime(spuId);

        // 计算 minPrice
        BigDecimal minPrice = null;
        for (SpuCreateDto.SkuCreateItem skuItem : dto.getSkuList()) {
            BigDecimal p = toBigDecimal(skuItem.getPrice());
            if (p != null) {
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
        // 新建草稿记一条状态流水（审计用；to_status=0 不被投放任务消费）
        spuStatusChanger.recordCreate(spuId, "MERCHANT", userId);

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
            sku.setPrice(toBigDecimal(skuItem.getPrice()));
            sku.setOriginalPrice(toBigDecimal(skuItem.getOriginalPrice()));
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
        // 编辑在售/下架商品 → 回草稿（走 changer 落状态流水；非状态字段已在上面 update 完成）
        if (spu.getStatus() != null
                && (spu.getStatus() == PmsSpu.STATUS_ON_SALE || spu.getStatus() == PmsSpu.STATUS_OFF_SHELF)) {
            spuStatusChanger.change(dto.getSpuId(), PmsSpu.STATUS_DRAFT, "EDIT_REGRESS", "MERCHANT", userId, null, null);
        }

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
        // 审核闸：「审核通过(5)」首次上架 或 「下架(2)」重新上架（不重审）；草稿(0)须先提交审核
        PmsStatus.PMS_SPU_NOT_AUDIT_PASSED.assertThrowResEx(
                spu.getStatus() == null
                || (spu.getStatus() != PmsSpu.STATUS_APPROVED && spu.getStatus() != PmsSpu.STATUS_OFF_SHELF));

        long now = System.currentTimeMillis();

        // 上架（走 changer 落状态流水）
        spuStatusChanger.change(spuId, PmsSpu.STATUS_ON_SALE, "LIST_ON", "MERCHANT", userId, null, u -> u.setPublishTime(now));

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

        // 下架（走 changer 落状态流水）
        long now = System.currentTimeMillis();
        spuStatusChanger.change(spuId, PmsSpu.STATUS_OFF_SHELF, "LIST_OFF", "MERCHANT", userId, null, null);

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
     * 商户提交审核（草稿 0 / 下架 2 → 待审核 4）。
     * <p>审核是唯一上架路径：商品须审核通过(5)后才能上架(1)。
     * 实际审核推进由 {@link com.clmcat.qianyu.mall.pms.scheduled.PmsSpuAuditTask} 完成（4 → 5，当前为自动通过 stub，
     * 未来替换为「自动审核 + 人工审核」）。下架后重新上架也必须再走审核。
     */
    @Transactional
    public void submitForAudit(long userId, Long spuId) {
        PmsSpu spu = spuServiceBiz.selectOneById(spuId);
        PmsStatus.PMS_SPU_NOT_FOUND.assertThrowResEx(spu == null || spu.getDeleted() == 1);
        Long merchantId = resolveMerchantId(userId);
        PmsStatus.PMS_SPU_NOT_OWNER.assertThrowResEx(
                spu.getMerchantId() == null || !spu.getMerchantId().equals(merchantId));
        // 仅草稿(0)/下架(2)可提交审核；审核中(4)/已通过(5)/在售(1)不允许重复提交
        int status = spu.getStatus() == null ? -1 : spu.getStatus();
        PmsStatus.PMS_SPU_STATUS_INVALID.assertThrowResEx(
                status != PmsSpu.STATUS_DRAFT && status != PmsSpu.STATUS_OFF_SHELF);

        // 提交审核（走 changer 落状态流水）
        spuStatusChanger.change(spuId, PmsSpu.STATUS_PENDING_AUDIT, "SUBMIT_AUDIT", "MERCHANT", userId, null, null);
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

    /** 安全 BigDecimal 解析：null/空串/异常→null（防 spuCreate 的 originalPrice="" 崩溃）。 */
    private static BigDecimal toBigDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Long resolveMerchantId(long userId) {
        // 商户身份门禁：必须已审核通过且生效（待审/冻结/禁用一律拒绝）——统一走 MerchantApi.requireActiveMerchant
        return merchantApi.requireActiveMerchant(userId).getId();
    }
}
