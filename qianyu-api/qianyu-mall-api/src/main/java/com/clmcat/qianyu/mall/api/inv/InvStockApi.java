package com.clmcat.qianyu.mall.api.inv;

import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;

import java.util.List;

public interface InvStockApi {

    InvStockDto getBySkuId(Long skuId);

    List<InvStockDto> batchQuery(List<Long> skuIds);

    boolean lockStock(String orderSn, List<InvStockDto.StockLockItem> items);

    boolean confirmStock(Long orderId);

    boolean releaseStock(String orderSn, List<InvStockDto.StockLockItem> items);

    /**
     * 商家库存调整（乐观锁）
     *
     * @param skuId       SKU ID
     * @param adjustType  调整类型：1-增加, 2-减少
     * @param quantity    调整数量
     * @param reason      调整原因
     * @return 调整后可用库存，失败返回 -1
     */
    int adjustStock(Long skuId, int adjustType, int quantity, String reason);
}
