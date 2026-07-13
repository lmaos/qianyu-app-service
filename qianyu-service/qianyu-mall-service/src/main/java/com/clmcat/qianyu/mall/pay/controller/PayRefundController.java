package com.clmcat.qianyu.mall.pay.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.qianyu.mall.pay.service.PayViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "PAY-回调与退款")
@ApiController
@RequestMapping("/api/mall")
public class PayRefundController {

    @Resource
    private PayViewServiceBiz payViewServiceBiz;

    /** S13: 微信支付 V3 异步通知——读 body + Wechatpay-* headers 传给 SDK 验签 */
    @Operation(summary = "微信支付异步通知")
    @PostMapping("/callback/wechat/payNotify")
    public String wechatPayNotify(HttpServletRequest request) {
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            return payViewServiceBiz.handleWechatCallback(body,
                    request.getHeader("Wechatpay-Serial"),
                    request.getHeader("Wechatpay-Nonce"),
                    request.getHeader("Wechatpay-Timestamp"),
                    request.getHeader("Wechatpay-Signature"));
        } catch (Exception e) {
            return "{\"code\":\"FAIL\",\"message\":\"读取回调body失败\"}";
        }
    }

    /** S13: 支付宝异步通知——读 form params 传给 SDK 验签 */
    @Operation(summary = "支付宝异步通知")
    @PostMapping("/callback/alipay/payNotify")
    public String alipayPayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> { if (v != null && v.length > 0) params.put(k, v[0]); });
        return payViewServiceBiz.handleAlipayCallback(params);
    }

    @Operation(summary = "微信退款异步通知")
    @PostMapping("/callback/wechat/refundNotify")
    public String wechatRefundNotify(HttpServletRequest request) {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    @Operation(summary = "支付宝退款异步通知")
    @PostMapping("/callback/alipay/refundNotify")
    public String alipayRefundNotify(HttpServletRequest request) {
        return "success";
    }
}
