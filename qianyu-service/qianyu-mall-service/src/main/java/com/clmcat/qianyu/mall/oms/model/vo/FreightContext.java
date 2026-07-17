package com.clmcat.qianyu.mall.oms.model.vo;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class FreightContext {
    private Long merchantId;
    private String province;
    private List<FreightItem> items;

    @Data
    @Builder
    public static class FreightItem {
        private Long spuId;
        private int quantity;
        private BigDecimal weight;
        private BigDecimal volume;
    }
}
