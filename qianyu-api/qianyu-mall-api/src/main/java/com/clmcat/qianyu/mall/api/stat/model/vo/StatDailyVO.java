package com.clmcat.qianyu.mall.api.stat.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StatDailyVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String date;        // yyyy-MM-dd
    private BigDecimal gmv;
    private long orderCount;
    private BigDecimal avgPrice;
}
