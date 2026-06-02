package com.clmcat.qianyu.mall.pay.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.pay.model.dto.RefundDTO;
import com.clmcat.qianyu.mall.pay.model.vo.RefundResultVO;
import com.clmcat.qianyu.mall.pay.rpc.PayRefundApiImpl;
import com.clmcat.qianyu.mall.pay.service.PayViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "PAY-回调与退款")
@ApiController
@RequestMapping("/api/mall")
public class PayRefundController {

    @Resource
    private PayViewServiceBiz payViewServiceBiz;

    @Resource
    private PayRefundApiImpl refundServiceBiz;

    @Operation(summary = "微信支付异步通知")
    @PostMapping("/callback/wechat/payNotify")
    public String wechatPayNotify(HttpServletRequest request) {
        return payViewServiceBiz.handleWechatCallback(null);
    }

    @Operation(summary = "支付宝异步通知")
    @PostMapping("/callback/alipay/payNotify")
    public String alipayPayNotify(HttpServletRequest request) {
        return payViewServiceBiz.handleAlipayCallback(null);
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

    @Operation(summary = "内部退款接口")
    @PostMapping("/internal/pay/refund")
    public RefundResultVO refund(@Params RefundDTO dto) {
        return refundServiceBiz.refund(dto);
    }
}
