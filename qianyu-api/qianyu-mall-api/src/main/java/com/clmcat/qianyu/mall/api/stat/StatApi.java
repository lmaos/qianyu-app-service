package com.clmcat.qianyu.mall.api.stat;

import com.clmcat.qianyu.mall.api.stat.model.vo.*;

import java.util.List;

/**
 * 平台统计 RPC 契约（供后台 AdminStatController 调用）。
 */
public interface StatApi {

    /** KPI 概览 */
    StatOverviewVO overview();

    /** GMV 趋势（近 N 天，按天聚合） */
    List<StatDailyVO> gmvTrend(int days);

    /** 订单状态分布 */
    List<StatDistVO> orderStatusDist();

    /** 支付渠道分布（近 N 天） */
    List<StatDistVO> payChannelDist(int days);

    /** 商户 GMV Top N */
    List<StatRankVO> topMerchants(int days, int limit);

    /** 商品销量 Top N（pms_spu.sales 预聚合） */
    List<StatRankVO> topProducts(int limit);

    // ===== Phase 2: 统计专页扩展 =====

    /** 售后趋势（近 N 天，按天） */
    List<StatDailyVO> aftersaleTrend(int days);

    /** 售后类型分布（1仅退款/2退货退款/3换货/4维修） */
    List<StatDistVO> aftersaleTypeDist();

    /** 售后状态分布 */
    List<StatDistVO> aftersaleStatusDist();

    /** 优惠券统计概览（各券发放/领取/核销） */
    List<StatRankVO> couponStats();

    /** 分类销售占比 */
    List<StatDistVO> categorySalesDist();

    // ===== Phase 3: 商家仪表盘增强 =====

    /** 商家仪表盘（增强版，含 7 天趋势 + Top 商品） */
    StatOverviewVO merchantDashboard(long merchantId);
}
