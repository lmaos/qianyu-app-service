package com.clmcat.qianyu.mall.api.stat.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StatOverviewVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private BigDecimal todayGMV;
    private long todayOrders;
    private long todayNewMerchants;
    private long pendingItems;
    private BigDecimal totalGMV;
    private long totalUsers;
    // Phase 3 商家仪表盘增强字段
    private BigDecimal todaySales;
    private long onShelfGoods;
    private BigDecimal shopScore;
    private long pendingShip;
    private long pendingAftersale;
    private List<StatDailyVO> sales7d;
    private List<StatRankVO> topProducts;
}
