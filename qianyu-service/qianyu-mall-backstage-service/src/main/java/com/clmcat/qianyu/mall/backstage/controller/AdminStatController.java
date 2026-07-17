package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.stat.StatApi;
import com.clmcat.qianyu.mall.api.stat.model.vo.*;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Tag(name = "后台-数据统计")
@ApiController
@RequestMapping("/api/admin/stat")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class AdminStatController {

    @DubboReference
    private StatApi statApi;

    @Operation(summary = "KPI 概览")
    @PostMapping("/overview")
    public StatOverviewVO overview(@Parameter(hidden = true) @Token Long adminId) {
        return statApi.overview();
    }

    @Operation(summary = "GMV 趋势")
    @PostMapping("/gmvTrend")
    public List<StatDailyVO> gmvTrend(@Parameter(hidden = true) @Token Long adminId,
                                      @Params Map<String, Object> params) {
        int days = params.get("days") != null ? Integer.parseInt(String.valueOf(params.get("days"))) : 30;
        return statApi.gmvTrend(days);
    }

    @Operation(summary = "订单状态分布")
    @PostMapping("/orderStatusDist")
    public List<StatDistVO> orderStatusDist(@Parameter(hidden = true) @Token Long adminId) {
        return statApi.orderStatusDist();
    }

    @Operation(summary = "支付渠道分布")
    @PostMapping("/payChannelDist")
    public List<StatDistVO> payChannelDist(@Parameter(hidden = true) @Token Long adminId,
                                           @Params Map<String, Object> params) {
        int days = params.get("days") != null ? Integer.parseInt(String.valueOf(params.get("days"))) : 7;
        return statApi.payChannelDist(days);
    }

    @Operation(summary = "商户 GMV Top N")
    @PostMapping("/topMerchants")
    public List<StatRankVO> topMerchants(@Parameter(hidden = true) @Token Long adminId,
                                         @Params Map<String, Object> params) {
        int days = params.get("days") != null ? Integer.parseInt(String.valueOf(params.get("days"))) : 30;
        int limit = params.get("limit") != null ? Integer.parseInt(String.valueOf(params.get("limit"))) : 10;
        return statApi.topMerchants(days, limit);
    }

    @Operation(summary = "商品销量 Top N")
    @PostMapping("/topProducts")
    public List<StatRankVO> topProducts(@Parameter(hidden = true) @Token Long adminId,
                                        @Params Map<String, Object> params) {
        int limit = params.get("limit") != null ? Integer.parseInt(String.valueOf(params.get("limit"))) : 10;
        return statApi.topProducts(limit);
    }

    // ===== Phase 2: 统计专页扩展 =====

    @Operation(summary = "售后趋势")
    @PostMapping("/aftersaleTrend")
    public List<StatDailyVO> aftersaleTrend(@Token Long adminId, @Params Map<String, Object> params) {
        int days = params.get("days") != null ? Integer.parseInt(String.valueOf(params.get("days"))) : 30;
        return statApi.aftersaleTrend(days);
    }

    @Operation(summary = "售后类型分布")
    @PostMapping("/aftersaleTypeDist")
    public List<StatDistVO> aftersaleTypeDist(@Token Long adminId) {
        return statApi.aftersaleTypeDist();
    }

    @Operation(summary = "售后状态分布")
    @PostMapping("/aftersaleStatusDist")
    public List<StatDistVO> aftersaleStatusDist(@Token Long adminId) {
        return statApi.aftersaleStatusDist();
    }

    @Operation(summary = "优惠券统计")
    @PostMapping("/couponStats")
    public List<StatRankVO> couponStats(@Token Long adminId) {
        return statApi.couponStats();
    }

    @Operation(summary = "分类销售占比")
    @PostMapping("/categorySalesDist")
    public List<StatDistVO> categorySalesDist(@Token Long adminId) {
        return statApi.categorySalesDist();
    }
}
