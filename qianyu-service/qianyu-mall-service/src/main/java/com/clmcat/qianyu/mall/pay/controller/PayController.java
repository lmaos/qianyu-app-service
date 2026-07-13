package com.clmcat.qianyu.mall.pay.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.pay.model.dto.PayApplyDTO;
import com.clmcat.qianyu.mall.pay.model.dto.PayResultQueryDTO;
import com.clmcat.qianyu.mall.pay.model.vo.PayApplyVO;
import com.clmcat.qianyu.mall.pay.model.vo.PayResultVO;
import com.clmcat.qianyu.mall.pay.service.PayViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "PAY-支付(C端)")
@ApiController
@RequestMapping("/api/mall/pay")
@LoginVerify
public class PayController {

    @Resource
    private PayViewServiceBiz payViewServiceBiz;

    @Resource
    private com.clmcat.qianyu.mall.pay.config.PayConfig payConfig;

    @Operation(summary = "支付配置(sandbox/渠道appId)")
    @PostMapping("/config")
    public java.util.Map<String, Object> config() {
        java.util.Map<String, Object> content = new java.util.LinkedHashMap<>();
        content.put("alipay", java.util.Map.of("appId", payConfig.getAlipay().getAppId()));
        content.put("wxpay", java.util.Map.of("appId", payConfig.getWxpay().getAppId()));
        content.put("sandbox", payConfig.getSandbox().isOpen());
        return content;
    }

    // app.md §11.4 /api/mall/pay/payApply
    @Operation(summary = "发起支付")
    @PostMapping("/payApply")
    public PayApplyVO payApply(@Parameter(hidden = true) @Token long userId, @Params PayApplyDTO dto) {
        return payViewServiceBiz.payApply(userId, dto);
    }

    // app.md §11.4 /api/mall/pay/payResult
    @Operation(summary = "支付结果查询")
    @PostMapping("/payResult")
    public PayResultVO payResult(@Parameter(hidden = true) @Token long userId, @Params PayResultQueryDTO dto) {
        return payViewServiceBiz.payResult(userId, dto);
    }
}
