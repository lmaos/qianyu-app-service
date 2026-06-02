package com.clmcat.qianyu.mall.pay.service.impl;

import com.clmcat.qianyu.mall.pay.rpc.PayPaymentApiImpl;
import com.clmcat.qianyu.mall.api.oms.OmsOrderApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.api.pay.model.dto.PayPaymentDto;
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

import java.math.BigDecimal;
import com.clmcat.qianyu.mall.pay.service.PayViewServiceBiz;

@Slf4j
@Service
public class PayViewServiceBizImpl implements PayViewServiceBiz {

    @Resource
    private PayPaymentApiImpl payServiceBiz;

    @DubboReference
    private OmsOrderApi omsOrderApi;

    public PayApplyVO payApply(Long userId, PayApplyDTO dto) {
        // 1. Check existing pending payment (idempotent)
        PayPayment existingPayment = getExistingPayment(dto.getOrderId());
        if (existingPayment != null && existingPayment.getPayStatus() == PayPayment.PAY_STATUS_PENDING) {
            return PayApplyVO.builder()
                    .paySn(existingPayment.getPaymentNo())
                    .payParams(null)
                    .build();
        }

        // 2. Create PayPayment record
        int channel = mapChannel(dto.getPayChannel());

        PayPayment payment = new PayPayment();
        payment.setId(PayChannelStrategy.PAYMENT_ID_SNOWFLAKE.nextId());
        payment.setPaymentNo("PAY" + System.currentTimeMillis() + String.format("%04d", (int) (payment.getId() % 10000)));
        payment.setOrderId(dto.getOrderId());
        payment.setUserId(userId);
        payment.setPayChannel(channel);
        payment.setPayType(mapChannelToPayType(channel));
        payment.setPayStatus(PayPayment.PAY_STATUS_PENDING);
        // Lookup order amount via OMS module
        BigDecimal amount = BigDecimal.ZERO;
        if (dto.getOrderId() != null) {
            OmsOrderDto orderDto = omsOrderApi.findById(dto.getOrderId());
            if (orderDto != null && orderDto.getPayAmount() != null) {
                amount = orderDto.getPayAmount();
            }
        }
        payment.setAmount(amount);
        payment.setCallbackStatus(PayPayment.CALLBACK_STATUS_NONE);
        payment.setCreateTime(System.currentTimeMillis());
        payment.setUpdateTime(System.currentTimeMillis());
        payment.setDeleted(0);

        // For balance payment, immediately succeed
        if (channel == PayPayment.CHANNEL_BALANCE) {
            payment.setPayStatus(PayPayment.PAY_STATUS_SUCCESS);
            payment.setPayTime(System.currentTimeMillis());
            payment.setUpdateTime(System.currentTimeMillis());
        }

        payServiceBiz.insert(payment);

        // For balance payment, update order status to pending_ship (20)
        if (channel == PayPayment.CHANNEL_BALANCE && dto.getOrderId() != null) {
            try {
                omsOrderApi.transitStatus(dto.getOrderId(), 10, 20);
            } catch (Exception e) {
                log.warn("余额支付后更新订单状态失败, orderId={}, error={}", dto.getOrderId(), e.getMessage());
            }
        }

        return PayApplyVO.builder()
                .paySn(payment.getPaymentNo())
                .payParams(null)
                .build();
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

        return PayResultVO.builder()
                .paySn(payment.getPaymentNo())
                .orderId(payment.getOrderId())
                .status(mapStatusToString(payment.getPayStatus()))
                .payChannel(mapChannelToString(payment.getPayChannel()))
                .payAmount(payment.getAmount().toPlainString())
                .payTime(payment.getPayTime() != null ? String.valueOf(payment.getPayTime()) : null)
                .build();
    }

    public String handleWechatCallback(String rawBody) {
        // Basic implementation: acknowledge callback
        // TODO: Verify sign, parse rawBody XML, update payment status, trigger order status
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    public String handleAlipayCallback(java.util.Map<String, String> params) {
        // Basic implementation: acknowledge callback
        // TODO: Verify sign, parse params, update payment status, trigger order status
        return "success";
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
        return switch (payChannel) {
            case "wechat" -> 1;
            case "alipay" -> 2;
            case "balance" -> 3;
            default -> 0;
        };
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
