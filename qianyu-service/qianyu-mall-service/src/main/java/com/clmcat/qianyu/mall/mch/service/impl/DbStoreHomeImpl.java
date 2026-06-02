package com.clmcat.qianyu.mall.mch.service.impl;

import com.clmcat.qianyu.mall.mch.mapper.MerchantMapper;
import com.clmcat.qianyu.mall.mch.mapper.MerchantStoreMapper;
import com.clmcat.qianyu.mall.mch.model.dto.StoreHomeQueryDTO;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantStore;
import com.clmcat.qianyu.mall.mch.model.vo.SpuSimpleVO;
import com.clmcat.qianyu.mall.mch.model.vo.StoreHomeVO;
import com.clmcat.qianyu.mall.mch.service.StoreHomeInterface;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsSpuTableDef.PMS_SPU;

/**
 * 店铺首页聚合查询 — 数据库实现
 */
@Component
public class DbStoreHomeImpl implements StoreHomeInterface {

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private MerchantStoreMapper storeMapper;

    @Resource
    private PmsSpuMapper spuMapper;

    @Resource
    private PmsSupport pmsSupport;

    @Override
    public StoreHomeVO queryByDto(StoreHomeQueryDTO dto) {
        Long merchantId = dto != null ? dto.getMerchantId() : null;
        int hotLimit = dto != null && dto.getHotLimit() != null ? dto.getHotLimit() : 6;
        int newLimit = dto != null && dto.getNewLimit() != null ? dto.getNewLimit() : 6;
        return query(merchantId, hotLimit, newLimit);
    }

    @Override
    public StoreHomeVO query(Long merchantId, int hotLimit, int newLimit) {
        if (merchantId == null) {
            return StoreHomeVO.builder()
                    .merchantId(null).spuCount(0).salesCount(0).score(new java.math.BigDecimal("0"))
                    .scoreText("暂无评分").goodsCountText("暂无商品")
                    .hotProducts(java.util.Collections.emptyList())
                    .newProducts(java.util.Collections.emptyList())
                    .build();
        }
        // 1. 商家基础信息
        Merchant merchant = merchantMapper.selectOneById(merchantId);
        String merchantName = merchant != null ? merchant.getName() : null;
        String merchantDesc = merchant != null ? merchant.getDescription() : null;

        // 2. 店铺信息
        MerchantStore store = storeMapper.selectByMerchantId(merchantId);
        String shopName = store != null && store.getName() != null ? store.getName() : merchantName;
        String shopLogo = store != null ? store.getLogo() : null;
        String shopBanner = store != null ? store.getCoverImage() : null;
        String description = store != null && store.getDescription() != null
                ? store.getDescription() : merchantDesc;
        Long storeId = store != null ? store.getId() : null;

        // 3. 在售商品数
        long spuCount = spuMapper.selectCountByQuery(
                QueryWrapper.create().where(PMS_SPU.MERCHANT_ID.eq(merchantId))
                        .and(PMS_SPU.STATUS.eq(1)).and(PMS_SPU.DELETED.eq(0)));

        // 聚合销量和评分
        int salesCount = 0;
        BigDecimal score = BigDecimal.ZERO;
        List<PmsSpu> allActiveSpus = spuMapper.selectListByQuery(
                QueryWrapper.create().where(PMS_SPU.MERCHANT_ID.eq(merchantId))
                        .and(PMS_SPU.STATUS.eq(1)).and(PMS_SPU.DELETED.eq(0)));
        for (PmsSpu spu : allActiveSpus) {
            if (spu.getSales() != null) {
                salesCount += spu.getSales();
            }
        }

        // 平均评分（只算有评分的商品）
        BigDecimal totalScore = BigDecimal.ZERO;
        int scoreCount = 0;
        for (PmsSpu spu : allActiveSpus) {
            if (spu.getAvgScore() != null && spu.getAvgScore().compareTo(BigDecimal.ZERO) > 0) {
                totalScore = totalScore.add(spu.getAvgScore());
                scoreCount++;
            }
        }
        if (scoreCount > 0) {
            score = totalScore.divide(BigDecimal.valueOf(scoreCount), 1, RoundingMode.HALF_UP);
        }

        String scoreText = score.compareTo(BigDecimal.ZERO) > 0
                ? score.setScale(1, RoundingMode.HALF_UP).toPlainString() : "暂无评分";

        // 在售商品数展示文本
        String goodsCountText = spuCount + "+";
        if (spuCount == 0) {
            goodsCountText = "暂无商品";
        }

        // 4. 热销商品
        List<SpuSimpleVO> hotProducts = new ArrayList<>();
        List<PmsSpu> hotList = spuMapper.selectListByQuery(
                QueryWrapper.create().where(PMS_SPU.MERCHANT_ID.eq(merchantId))
                        .and(PMS_SPU.STATUS.eq(1)).and(PMS_SPU.DELETED.eq(0))
                        .orderBy(PMS_SPU.SALES.desc()).limit(hotLimit));
        for (PmsSpu spu : hotList) {
            hotProducts.add(toSpuSimple(spu));
        }

        // 5. 最新商品
        List<SpuSimpleVO> newProducts = new ArrayList<>();
        List<PmsSpu> newList = spuMapper.selectListByQuery(
                QueryWrapper.create().where(PMS_SPU.MERCHANT_ID.eq(merchantId))
                        .and(PMS_SPU.STATUS.eq(1)).and(PMS_SPU.DELETED.eq(0))
                        .orderBy(PMS_SPU.CREATE_TIME.desc()).limit(newLimit));
        for (PmsSpu spu : newList) {
            newProducts.add(toSpuSimple(spu));
        }

        return StoreHomeVO.builder()
                .merchantId(merchantId)
                .storeId(storeId)
                .shopName(shopName)
                .shopLogo(shopLogo)
                .shopBanner(shopBanner)
                .description(description)
                .spuCount((int) spuCount)
                .salesCount(salesCount)
                .score(score)
                .scoreText(scoreText)
                .goodsCountText(goodsCountText)
                .hotProducts(hotProducts)
                .newProducts(newProducts)
                .build();
    }

    private SpuSimpleVO toSpuSimple(PmsSpu spu) {
        String image = spu.getThumbImage() != null ? spu.getThumbImage() : spu.getMainImage();
        String price = spu.getMinPrice() != null
                ? spu.getMinPrice().setScale(2, RoundingMode.HALF_UP).toPlainString() : null;
        String originalPrice = null;
        try {
            BigDecimal op = pmsSupport.getDefaultSkuOriginalPrice(spu.getId());
            if (op != null) {
                originalPrice = op.setScale(2, RoundingMode.HALF_UP).toPlainString();
            }
        } catch (Exception e) {
            // ignore
        }
        return SpuSimpleVO.builder()
                .id(spu.getId())
                .name(spu.getName())
                .mainImage(image)
                .price(price)
                .originalPrice(originalPrice)
                .sales(spu.getSales() != null ? spu.getSales() : 0)
                .build();
    }
}
