package com.clmcat.qianyu.mall.api.pms;

import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSkuDto;

import java.util.Collection;
import java.util.List;

public interface PmsSkuApi {

    PmsSkuDto getById(Long skuId);

    List<PmsSkuDto> listBySpuId(Long spuId);

    /** S16 新增：按商家 ID 查其全部 SKU，供库存/物流域做商家归属过滤。 */
    List<PmsSkuDto> listByMerchantId(Long merchantId);

    /**
     * PriceService 用：批量取 SKU（替 N 次 getById），供下单算价。
     *
     * @param skuIds SKU ID 集合
     * @return SKU DTO 列表（不存在的跳过）
     */
    List<PmsSkuDto> listByIds(Collection<Long> skuIds);
}
