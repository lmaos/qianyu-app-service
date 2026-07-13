package com.clmcat.qianyu.mall.pay.service.impl;

import com.clmcat.qianyu.mall.pay.config.PayConfig;
import com.clmcat.qianyu.mall.pay.rpc.PayPaymentApiImpl;
import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.oms.OmsOrderApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.api.pay.model.dto.PayPaymentDto;
import com.clmcat.qianyu.mall.oms.model.entity.status.OmsStatus;
import com.clmcat.qianyu.mall.pay.model.dto.PayApplyDTO;
import com.clmcat.qianyu.mall.pay.model.dto.PayResultQueryDTO;
import com.clmcat.qianyu.mall.pay.model.entity.PayPayment;
import com.clmcat.qianyu.mall.pay.model.entity.status.PayStatus;
import com.clmcat.qianyu.mall.pay.model.vo.PayApplyVO;
import com.clmcat.qianyu.mall.pay.model.vo.PayResultVO;
import com.clmcat.qianyu.mall.pay.support.PayChannelStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import com.clmcat.qianyu.mall.pay.service.PayViewServiceBiz;

@Slf4j
@Service
public class PayViewServiceBizImpl implements PayViewServiceBiz {

    @Resource
    private PayPaymentApiImpl payServiceBiz;

    @DubboReference
    private OmsOrderApi omsOrderApi;

    @DubboReference
    private InvStockApi invStockApi;

    @Resource
    private com.clmcat.qianyu.mall.pay.config.PayConfig payConfig;

    @Transactional(rollbackFor = Exception.class)
    public PayApplyVO payApply(Long userId, PayApplyDTO dto) {
        // S5(#6): 订单存在 + 归属 + 状态校验（仅待付款 10 可发起支付）
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(dto.getOrderId() == null);
        OmsOrderDto orderDto = omsOrderApi.findById(dto.getOrderId());
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(orderDto == null);
        OmsStatus.OMS_ORDER_NOT_BELONG_USER.assertThrowResEx(
                !Long.valueOf(userId).equals(orderDto.getUserId()));
        OmsStatus.OMS_ORDER_STATUS_ERROR.assertThrowResEx(
                orderDto.getStatus() == null || orderDto.getStatus() != 10);

        // 1. 已有 PENDING 支付单幂等返回
        PayPayment existingPayment = getExistingPayment(dto.getOrderId());
        if (existingPayment != null && existingPayment.getPayStatus() == PayPayment.PAY_STATUS_PENDING) {
            return PayApplyVO.builder().paySn(existingPayment.getPaymentNo()).payParams(null).build();
        }
        // S5(#6): 已 SUCCESS 的订单不可重复发起支付
        PayStatus.PAY_ORDER_ALREADY_PAID.assertThrowResEx(
                existingPayment != null && existingPayment.getPayStatus() == PayPayment.PAY_STATUS_SUCCESS);

        // 2. Create PayPayment record
        int channel = mapChannel(dto.getPayChannel());
        PayPayment payment = new PayPayment();
        payment.setId(PayChannelStrategy.PAYMENT_ID_SNOWFLAKE.nextId());
        payment.setPaymentNo("PAY" + System.currentTimeMillis() + String.format("%04d", (int) (payment.getId() % 10000)));
        payment.setOrderId(dto.getOrderId());
        payment.setUserId(userId);
        payment.setMerchantId(orderDto.getMerchantId());
        payment.setPayChannel(channel);
        payment.setPayType(mapChannelToPayType(channel));
        payment.setPayStatus(PayPayment.PAY_STATUS_PENDING);
        payment.setAmount(orderDto.getPayAmount() != null ? orderDto.getPayAmount() : BigDecimal.ZERO);
        // S5: 防 0 元支付（订单金额缺失不应静默成功）
        PayStatus.PAY_AMOUNT_MISMATCH.assertThrowResEx(
                payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0);
        payment.setCallbackStatus(PayPayment.CALLBACK_STATUS_NONE);
        payment.setCreateTime(System.currentTimeMillis());
        payment.setUpdateTime(System.currentTimeMillis());
        payment.setDeleted(0);

        // ── Sandbox 沙箱模式：不拉起第三方支付，按配置直接成功/失败 ──
        if (payConfig.getSandbox().isOpen() && !"realmode".equalsIgnoreCase(payConfig.getSandbox().getMode())) {
            boolean success = "success".equalsIgnoreCase(payConfig.getSandbox().getMode());
            payment.setPayStatus(success ? PayPayment.PAY_STATUS_SUCCESS : PayPayment.PAY_STATUS_FAILED);
            payment.setPayTime(System.currentTimeMillis());
            payment.setUpdateTime(System.currentTimeMillis());
            if (success) {
                payServiceBiz.insert(payment);
                paySuccessPostProcess(dto.getOrderId());
            } else {
                // S6(#18): FAILED 单独立事务持久化（不被外层回滚），再抛错
                payServiceBiz.insertFailedPayment(payment);
                PayStatus.PAY_SANDBOX_FAIL.assertThrowResEx(true);
            }
            return PayApplyVO.builder().paySn(payment.getPaymentNo()).payParams(null).build();
        }

        // 余额支付即时成功
        if (channel == PayPayment.CHANNEL_BALANCE) {
            payment.setPayStatus(PayPayment.PAY_STATUS_SUCCESS);
            payment.setPayTime(System.currentTimeMillis());
            payment.setUpdateTime(System.currentTimeMillis());
            payServiceBiz.insert(payment);
            paySuccessPostProcess(dto.getOrderId());
            return PayApplyVO.builder().paySn(payment.getPaymentNo()).payParams(null).build();
        }

        // 真实渠道（wechat/alipay）：PENDING 落库，等回调
        payServiceBiz.insert(payment);
        return PayApplyVO.builder().paySn(payment.getPaymentNo()).payParams(null).build();
    }

    /**
     * S5 + S4: 支付成功后处理——CAS 推进订单 10→20（校验返回值）+ 核销库存。
     * transitStatus 返回 false 表示订单已被并发取消，抛错回滚本事务（支付单随之回滚，sandbox/余额无真实扣款）。
     */
    private void paySuccessPostProcess(Long orderId) {
        boolean ok = omsOrderApi.markPaid(orderId);
        if (!ok) {
            log.error("支付成功但订单状态非待付款(并发取消?) orderId={}", orderId);
            OmsStatus.OMS_ORDER_ALREADY_CANCELLED.assertThrowResEx(true);
        }
        // S4: 核销锁定库存（locked→实扣），失败不阻断支付（钱已收，记悬挂待对账）
        try {
            invStockApi.confirmStock(orderId);
        } catch (Exception e) {
            log.warn("confirmStock 失败 orderId={} error={}", orderId, e.getMessage());
        }
    }

    public PayResultVO payResult(Long userId, PayResultQueryDTO dto) {
        PayPayment payment = null;
        if (dto.getPaySn() != null) {
            payment = payServiceBiz.getPaymentByPaymentNo(dto.getPaySn());
        } else if (dto.getOrderId() != null) {
            // Get latest by orderId
            PayPaymentDto paymentDto = payServiceBiz.findLatestByOrderId(dto.getOrderId());
            if (paymentDto != null) {
                payment = payServiceBiz.getPaymentByPaymentNo(paymentDto.getPaymentNo());
            }
        }

        PayStatus.PAY_ORDER_NOT_FOUND.assertThrowResEx(payment == null);
        // S5: 归属校验——仅订单所属用户可查支付单
        OmsStatus.OMS_ORDER_NOT_BELONG_USER.assertThrowResEx(
                !Long.valueOf(userId).equals(payment.getUserId()));

        return PayResultVO.builder()
                .paySn(payment.getPaymentNo())
                .orderId(payment.getOrderId())
                .status(mapStatusToString(payment.getPayStatus()))
                .payChannel(mapChannelToString(payment.getPayChannel()))
                .payAmount(payment.getAmount().toPlainString())
                .payTime(payment.getPayTime() != null ? String.valueOf(payment.getPayTime()) : null)
                .build();
    }

    /**
     * S13: 微信支付 V3 异步通知回调。使用官方 wechatpay-java SDK 验签 + 解密。
     * 验签成功后 CAS 更新 PayPayment PENDING→SUCCESS + markPaid 推进订单。
     */
    public String handleWechatCallback(String rawBody, String serial, String nonce, String timestamp, String signature) {
        PayConfig.Wxpay wx = payConfig.getWxpay();
        if (wx.getMerchantId() == null || wx.getMerchantId().isEmpty() || "demo-wxpay".equals(wx.getAppId())) {
            log.warn("微信支付回调：商户凭证未配置（demo），跳过验签（开发模式）");
            return "{\"code\":\"SUCCESS\",\"message\":\"OK(dev mode)\"}";
        }
        try {
            var certConfig = new com.wechat.pay.java.core.RSAAutoCertificateConfig.Builder()
                    .merchantId(wx.getMerchantId()).privateKeyFromPath(wx.getPrivateKey())
                    .merchantSerialNumber(wx.getMerchantSerial()).apiV3Key(wx.getApiV3Key()).build();
            var parser = new com.wechat.pay.java.core.notification.NotificationParser(certConfig);
            var requestParam = new com.wechat.pay.java.core.notification.RequestParam.Builder()
                    .serialNumber(serial).nonce(nonce).signature(signature).timestamp(timestamp).body(rawBody).build();
            var txn = parser.parse(requestParam, com.wechat.pay.java.service.payments.model.Transaction.class);
            log.info("微信支付回调验签成功: outTradeNo={} txnId={}", txn.getOutTradeNo(), txn.getTransactionId());
            updatePaymentOnCallback(txn.getOutTradeNo(), txn.getTransactionId(), rawBody);
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            log.error("微信支付回调验签失败: {}", e.getMessage());
            return "{\"code\":\"FAIL\",\"message\":\"验签失败\"}";
        }
    }

    /** S13: 微信回调兼容旧签名（仅 body，headers 从 request 提取） */
    public String handleWechatCallback(String rawBody) {
        return handleWechatCallback(rawBody, null, null, null, null);
    }

    /**
     * S13: 支付宝异步通知回调。使用官方 alipay-sdk-java 的 AlipaySignature.rsaCheckV1 验签。
     */
    public String handleAlipayCallback(java.util.Map<String, String> params) {
        PayConfig.Alipay ali = payConfig.getAlipay();
        if (ali.getPublicKey() == null || ali.getPublicKey().isEmpty() || "demo-alipay".equals(ali.getAppId())) {
            log.warn("支付宝回调：公钥未配置（demo），跳过验签（开发模式）");
            return "success";
        }
        try {
            boolean signVerified = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                    params, ali.getPublicKey(), "UTF-8", ali.getSignType());
            if (!signVerified) { log.error("支付宝回调验签失败"); return "fail"; }
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            log.info("支付宝回调验签成功: outTradeNo={} tradeNo={} status={}", outTradeNo, tradeNo, tradeStatus);
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                updatePaymentOnCallback(outTradeNo, tradeNo, params.toString());
            }
            return "success";
        } catch (Exception e) {
            log.error("支付宝回调处理失败: {}", e.getMessage());
            return "fail";
        }
    }

    /** S13: 回调验签成功后更新支付单 + 推进订单 */
    private void updatePaymentOnCallback(String paymentNo, String transactionId, String callbackData) {
        try {
            PayPayment payment = payServiceBiz.getPaymentByPaymentNo(paymentNo);
            if (payment == null || payment.getPayStatus() != PayPayment.PAY_STATUS_PENDING) return;
            long now = System.currentTimeMillis();
            payment.setPayStatus(PayPayment.PAY_STATUS_SUCCESS);
            payment.setPayTime(now);
            payment.setTransactionId(transactionId);
            payment.setCallbackData(callbackData);
            payment.setUpdateTime(now);
            payServiceBiz.update(payment);
            log.info("回调更新支付单 SUCCESS: paymentNo={} → markPaid", paymentNo);
            paySuccessPostProcess(payment.getOrderId());
        } catch (Exception e) {
            log.error("回调更新异常 paymentNo={} error={}", paymentNo, e.getMessage());
        }
    }

    private PayPayment getExistingPayment(Long orderId) {
        PayPaymentDto dto = payServiceBiz.findPendingByOrderId(orderId);
        if (dto != null) {
            return payServiceBiz.getPaymentByPaymentNo(dto.getPaymentNo());
        }
        return null;
    }

    private String mapChannelToString(Integer payChannel) {
        if (payChannel == null) return "";
        return switch (payChannel) {
            case 1 -> "wechat";
            case 2 -> "alipay";
            case 3 -> "balance";
            default -> "";
        };
    }

    private String mapStatusToString(Integer payStatus) {
        if (payStatus == null) return "PENDING";
        return switch (payStatus) {
            case 10 -> "PENDING";
            case 20 -> "SUCCESS";
            case 30 -> "FAILED";
            case 40 -> "CLOSED";
            default -> "PENDING";
        };
    }

    private int mapChannel(String payChannel) {
        // S11: 兼容前端传数字字符串(1/2/3)与语义字符串(wechat/alipay/balance)；未知渠道显式拦截杜绝静默 PENDING
        if ("wechat".equals(payChannel) || "1".equals(payChannel)) return 1;
        if ("alipay".equals(payChannel) || "2".equals(payChannel)) return 2;
        if ("balance".equals(payChannel) || "3".equals(payChannel)) return 3;
        PayStatus.PAY_CHANNEL_NOT_SUPPORT.assertThrowResEx(true);
        return 0; // unreachable（assertThrowResEx 抛错）
    }

    private int mapChannelToPayType(int channel) {
        return switch (channel) {
            case PayPayment.CHANNEL_WECHAT -> PayPayment.TYPE_WECHAT_APP;
            case PayPayment.CHANNEL_ALIPAY -> PayPayment.TYPE_ALIPAY_APP;
            case PayPayment.CHANNEL_BALANCE -> PayPayment.TYPE_BALANCE;
            default -> 0;
        };
    }
}
