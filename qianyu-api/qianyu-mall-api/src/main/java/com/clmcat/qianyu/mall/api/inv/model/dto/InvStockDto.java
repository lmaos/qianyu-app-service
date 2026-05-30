package com.clmcat.qianyu.mall.api.inv.model.dto;

import lombok.Data;
import java.io.Serializable;

import java.util.List;

@Data
public class InvStockDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long skuId;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer totalStock;

    @Data
    public static class StockLockItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long skuId;
        private Integer quantity;
    }
}
