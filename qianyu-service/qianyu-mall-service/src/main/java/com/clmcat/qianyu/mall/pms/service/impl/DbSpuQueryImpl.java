package com.clmcat.qianyu.mall.pms.service.impl;

import com.clmcat.qianyu.mall.pms.model.dto.SpuCategorySearchDto;
import com.clmcat.qianyu.mall.pms.model.enums.SortMode;
import com.clmcat.qianyu.mall.pms.mapper.PmsSkuMapper;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSku;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.vo.SpuListItemVo;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.clmcat.qianyu.mall.pms.service.SpuQueryInterface;

import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsSpuTableDef.PMS_SPU;
import static com.clmcat.qianyu.mall.mch.model.entity.table.MerchantTableDef.MERCHANT;

/**
 * 商品查询 — 数据库实现
 */
@Component
public class DbSpuQueryImpl implements SpuQueryInterface {

    private static final String PRICE_DIRECTION_ASC = "asc";
    private static final String PRICE_DIRECTION_DESC = "desc";

    @Resource
    private PmsSpuMapper spuMapper;

    @Resource
    private PmsSkuMapper skuMapper;

    @Resource
    private com.clmcat.qianyu.mall.mch.mapper.MerchantMapper merchantMapper;

    @Override
    public Page<SpuListItemVo> queryByCategory(SpuCategorySearchDto dto) {
        Long categoryId = dto != null ? dto.getCategoryId() : null;
        String sortMode = dto != null ? dto.getSortMode() : null;
        String priceDirection = dto != null ? dto.getPriceDirection() : null;
        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create()
                .where(PMS_SPU.STATUS.eq(1));

        if (categoryId != null) {
            qw.and(PMS_SPU.CATEGORY_ID.eq(categoryId));
        }

        // 排序
        applySort(qw, sortMode, priceDirection);

        Page<PmsSpu> spuPage = spuMapper.paginate(new Page<>(pageNum, pageSize), qw);
        if (spuPage == null) {
            return new Page<SpuListItemVo>();
        }

        // 批量查 merchant name
        Map<Long, String> merchantNameMap = resolveMerchantNames(spuPage.getRecords());

        return spuPage.map(spu -> toListItemVo(spu, merchantNameMap));
    }

    private void applySort(QueryWrapper qw, String sortMode, String priceDirection) {
        SortMode mode = SortMode.of(sortMode);
        switch (mode) {
            case SALES:
                qw.orderBy(PMS_SPU.SALES.desc());
                break;
            case PRICE:
                if (PRICE_DIRECTION_ASC.equals(priceDirection)) {
                    qw.orderBy(PMS_SPU.MIN_PRICE.asc());
                } else {
                    qw.orderBy(PMS_SPU.MIN_PRICE.desc());
                }
                break;
            default:
                // RECOMMEND: 综合排序 — 销量优先，评分辅助
                qw.orderBy(PMS_SPU.SALES.desc(), PMS_SPU.AVG_SCORE.desc());
                break;
        }
    }

    private SpuListItemVo toListItemVo(PmsSpu spu, Map<Long, String> merchantNameMap) {
        String mainImage = spu.getThumbImage() != null ? spu.getThumbImage() : spu.getMainImage();
        String price = formatAmount(spu.getMinPrice());
        String originalPrice = resolveOriginalPrice(spu.getId());
        String shopName = merchantNameMap.getOrDefault(spu.getMerchantId(), null);

        return SpuListItemVo.builder()
                .id(spu.getId())
                .title(spu.getName())
                .mainImage(mainImage)
                .price(price)
                .originalPrice(originalPrice)
                .shopName(shopName)
                .categoryId(spu.getCategoryId())
                .build();
    }

    /**
     * 批量查 merchant name
     */
    private Map<Long, String> resolveMerchantNames(List<PmsSpu> spuList) {
        if (spuList == null || spuList.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> merchantIds = spuList.stream()
                .map(PmsSpu::getMerchantId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<com.clmcat.qianyu.mall.mch.model.entity.Merchant> merchants = merchantMapper.selectListByQuery(
                QueryWrapper.create().where(MERCHANT.ID.in(merchantIds))
        );
        Map<Long, String> map = new HashMap<>();
        for (com.clmcat.qianyu.mall.mch.model.entity.Merchant m : merchants) {
            map.put(m.getId(), m.getName());
        }
        return map;
    }

    /**
     * 获取默认 SKU 的原价作为划线价
     */
    private String resolveOriginalPrice(Long spuId) {
        PmsSku defaultSku = skuMapper.selectDefaultBySpuId(spuId);
        return formatAmount(defaultSku != null ? defaultSku.getOriginalPrice() : null);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
