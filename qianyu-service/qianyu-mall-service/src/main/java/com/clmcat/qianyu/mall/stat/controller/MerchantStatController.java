package com.clmcat.qianyu.mall.stat.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.stat.StatApi;
import com.clmcat.qianyu.mall.api.stat.model.vo.StatOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "商家数据统计")
@ApiController
@RequestMapping("/api/mall/merchant/stat")
@LoginVerify
public class MerchantStatController {

    @DubboReference
    private StatApi statApi;

    @DubboReference
    private MerchantApi merchantApi;

    @Operation(summary = "商家仪表盘（增强版）")
    @PostMapping("/dashboard")
    public StatOverviewVO dashboard(@Parameter(hidden = true) @Token long userId) {
        Long merchantId = merchantApi.requireActiveMerchant(userId).getId();
        return statApi.merchantDashboard(merchantId);
    }
}
