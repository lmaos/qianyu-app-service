package com.clmcat.qianyu.mall.pay.rpc;

import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.oms.OmsOrderApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderItemDto;
import com.clmcat.qianyu.mall.api.pay.PayRefundApi;
import com.clmcat.qianyu.mall.api.pay.model.dto.PayRefundDto;
import com.clmcat.qianyu.mall.pay.mapper.PayRefundMapper;
import com.clmcat.qianyu.mall.pay.model.dto.RefundDTO;
import com.clmcat.qianyu.mall.pay.model.entity.PayPayment;
import com.clmcat.qianyu.mall.pay.model.entity.PayRefund;
import com.clmcat.qianyu.mall.pay.model.entity.status.PayStatus;
import com.clmcat.qianyu.mall.pay.model.vo.RefundResultVO;
import com.clmcat.qianyu.mall.pay.support.PayChannelStrategy;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@DubboService
@Service
public class PayRefundApiImpl implements PayRefundApi {

    @Resource
    private PayRefundMapper refundMapper;

    @Resource
    private PayPaymentApiImpl payServiceBiz;

    @DubboReference
    private InvStockApi invStockApi;

    @DubboReference
    private OmsOrderApi omsOrderApi;

    @Transactional(rollbackFor = Exception.class)
    public RefundResultVO refund(RefundDTO dto) {
        PayPayment payment = payServiceBiz.getPaymentByPaymentNo(dto.getPaySn());
        PayStatus.PAY_ORDER_NOT_FOUND.assertThrowResEx(payment == null);
        PayStatus.PAY_ORDER_STATUS_ERROR.assertThrowResEx(
                payment.getPayStatus() != PayPayment.PAY_STATUS_SUCCESS);

        BigDecimal refundAmount = new BigDecimal(dto.getRefundAmount());
        PayStatus.PAY_REFUND_AMOUNT_EXCEED.assertThrowResEx(
                refundAmount.compareTo(payment.getAmount()) > 0);

        // Check cumulative refunds
        BigDecimal totalRefunded = sumExistingRefunds(payment.getId());
        PayStatus.PAY_REFUND_AMOUNT_EXCEED.assertThrowResEx(
                totalRefunded.add(refundAmount).compareTo(payment.getAmount()) > 0);

        PayRefund refund = new PayRefund();
        refund.setId(PayChannelStrategy.REFUND_ID_SNOWFLAKE.nextId());
        refund.setRefundNo(dto.getOutRefundNo());
        refund.setPaymentId(payment.getId());
        refund.setOrderId(payment.getOrderId());
        refund.setAmount(refundAmount);
        refund.setReason(dto.getRefundReason());
        refund.setRefundChannel(payment.getPayChannel() == PayPayment.CHANNEL_BALANCE
                ? PayRefund.REFUND_CHANNEL_BALANCE : PayRefund.REFUND_CHANNEL_ORIGINAL);
        refund.setRefundStatus(PayRefund.REFUND_STATUS_PENDING);
        refund.setCreateTime(System.currentTimeMillis());
        refund.setUpdateTime(System.currentTimeMillis());
        refund.setDeleted(0);
        refundMapper.insertSelective(refund);

        // TODO: Call actual refund channel (wechat/alipay/balance)
        // For balance: immediate success
        if (payment.getPayChannel() == PayPayment.CHANNEL_BALANCE) {
            refund.setRefundStatus(PayRefund.REFUND_STATUS_SUCCESS);
            refund.setRefundTime(System.currentTimeMillis());
            refundMapper.update(refund);
            // S21: 退款成功释放订单锁定库存（单一释放点；releaseStock CAS，幂等日志去重依赖 S12）
            releaseStockForOrder(payment.getOrderId());
        }

        return RefundResultVO.builder()
                .refundSn(refund.getRefundNo())
                .status(mapRefundStatusToString(refund.getRefundStatus()))
                .build();
    }

    @Override
    public void insert(PayRefundDto dto) {
        refundMapper.insertSelective(toEntity(dto));
    }

    /**
     * S21: 退款成功释放订单库存（单一释放点）。仅退款即时释放；退货退款应在商家确认收货后释放。
     * <p>注：confirmStock 后库存已扣减（locked=0），releaseStock CAS 会跳过；sold→available 回库需新原语（后续增强）。
     */
    private void releaseStockForOrder(Long orderId) {
        try {
            OmsOrderDto order = omsOrderApi.findById(orderId);
            if (order == null) return;
            List<OmsOrderItemDto> items = omsOrderApi.findOrderItemsByOrderId(orderId);
            if (items == null || items.isEmpty()) return;
            List<InvStockDto.StockLockItem> releaseItems = new ArrayList<>();
            for (OmsOrderItemDto it : items) {
                InvStockDto.StockLockItem li = new InvStockDto.StockLockItem();
                li.setSkuId(it.getSkuId());
                li.setQuantity(it.getQuantity());
                releaseItems.add(li);
            }
            invStockApi.releaseStock(order.getOrderNo(), releaseItems);
        } catch (Exception e) {
            log.warn("退款释放库存失败 orderId={} error={}", orderId, e.getMessage());
        }
    }

    @Override
    public PayRefundDto findByRefundNo(String refundNo) {
        PayRefund refund = refundMapper.selectOneByQuery(
                QueryWrapper.create().where("refund_no = ?", refundNo).and("deleted = 0"));
        return toDto(refund);
    }

    private BigDecimal sumExistingRefunds(Long paymentId) {
        List<PayRefund> refunds = refundMapper.selectListByQuery(
                QueryWrapper.create().where("payment_id = ?", paymentId)
                        .and("refund_status IN (10, 20)").and("deleted = 0"));
        return refunds.stream().map(PayRefund::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String mapRefundStatusToString(Integer refundStatus) {
        if (refundStatus == null) return "PROCESSING";
        return switch (refundStatus) {
            case 10 -> "PROCESSING";
            case 20 -> "SUCCESS";
            case 30 -> "FAILED";
            default -> "PROCESSING";
        };
    }

    private PayRefundDto toDto(PayRefund entity) {
        if (entity == null) return null;
        PayRefundDto dto = new PayRefundDto();
        dto.setId(entity.getId());
        dto.setRefundNo(entity.getRefundNo());
        dto.setPaymentId(entity.getPaymentId());
        dto.setOrderId(entity.getOrderId());
        dto.setAfterSaleId(entity.getAfterSaleId());
        dto.setAmount(entity.getAmount());
        dto.setReason(entity.getReason());
        dto.setRefundChannel(entity.getRefundChannel());
        dto.setRefundStatus(entity.getRefundStatus());
        dto.setTransactionId(entity.getTransactionId());
        dto.setRefundTime(entity.getRefundTime());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    private PayRefund toEntity(PayRefundDto dto) {
        PayRefund entity = new PayRefund();
        entity.setId(dto.getId());
        entity.setRefundNo(dto.getRefundNo());
        entity.setPaymentId(dto.getPaymentId());
        entity.setOrderId(dto.getOrderId());
        entity.setAfterSaleId(dto.getAfterSaleId());
        entity.setAmount(dto.getAmount());
        entity.setReason(dto.getReason());
        entity.setRefundChannel(dto.getRefundChannel());
        entity.setRefundStatus(dto.getRefundStatus());
        entity.setTransactionId(dto.getTransactionId());
        entity.setRefundTime(dto.getRefundTime());
        entity.setCreateTime(dto.getCreateTime());
        return entity;
    }
}
