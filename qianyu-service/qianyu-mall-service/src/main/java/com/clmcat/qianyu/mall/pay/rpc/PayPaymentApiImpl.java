package com.clmcat.qianyu.mall.pay.rpc;

import com.clmcat.qianyu.mall.api.pay.PayPaymentApi;
import com.clmcat.qianyu.mall.api.pay.model.dto.PayPaymentDto;
import com.clmcat.qianyu.mall.pay.mapper.PayPaymentMapper;
import com.clmcat.qianyu.mall.pay.model.entity.PayPayment;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DubboService
@Service
public class PayPaymentApiImpl implements PayPaymentApi {

    @Resource
    private PayPaymentMapper paymentMapper;

    public void insert(PayPayment payment) {
        paymentMapper.insertSelective(payment);
    }

    /**
     * S6(#18): 失败支付单独立事务持久化。REQUIRES_NEW 保证外层 payApply 抛错回滚时 FAILED 单仍落库可追溯。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void insertFailedPayment(PayPayment payment) {
        paymentMapper.insertSelective(payment);
    }

    /**
     * S6(#8): CAS 关闭订单的 PENDING 支付单（PENDING→CLOSED）。订单取消时调用，使悬挂待支付单失效。
     */
    @Override
    public int closePendingByOrderId(Long orderId) {
        if (orderId == null) return 0;
        PayPayment update = new PayPayment();
        update.setPayStatus(PayPayment.PAY_STATUS_CLOSED);
        update.setUpdateTime(System.currentTimeMillis());
        return paymentMapper.updateByQuery(update,
                QueryWrapper.create().where("order_id = ?", orderId)
                        .and("pay_status = ?", PayPayment.PAY_STATUS_PENDING)
                        .and("deleted = 0"));
    }

    public void update(PayPayment payment) {
        paymentMapper.update(payment);
    }

    @Override
    public PayPaymentDto findByPaymentNo(String paymentNo) {
        PayPayment payment = paymentMapper.selectOneByQuery(
                QueryWrapper.create().where("payment_no = ?", paymentNo).and("deleted = 0"));
        return toDto(payment);
    }

    @Override
    public PayPaymentDto findLatestByOrderId(Long orderId) {
        PayPayment payment = paymentMapper.selectOneByQuery(
                QueryWrapper.create().where("order_id = ?", orderId).and("deleted = 0")
                        .orderBy("create_time", false).limit(1));
        return toDto(payment);
    }

    @Override
    public PayPaymentDto findPendingByOrderId(Long orderId) {
        PayPayment payment = paymentMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("order_id = ?", orderId)
                        .and("pay_status = ?", PayPayment.PAY_STATUS_PENDING)
                        .and("deleted = 0")
                        .orderBy("create_time", false).limit(1));
        return toDto(payment);
    }

    /**
     * Internal helper - get payment entity by paymentNo
     */
    public PayPayment getPaymentByPaymentNo(String paymentNo) {
        return paymentMapper.selectOneByQuery(
                QueryWrapper.create().where("payment_no = ?", paymentNo).and("deleted = 0"));
    }

    private PayPaymentDto toDto(PayPayment payment) {
        if (payment == null) return null;
        PayPaymentDto dto = new PayPaymentDto();
        dto.setId(payment.getId());
        dto.setPaymentNo(payment.getPaymentNo());
        dto.setOrderId(payment.getOrderId());
        dto.setUserId(payment.getUserId());
        dto.setMerchantId(payment.getMerchantId());
        dto.setAmount(payment.getAmount());
        dto.setPayChannel(payment.getPayChannel());
        dto.setPayType(payment.getPayType());
        dto.setPayStatus(payment.getPayStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCallbackStatus(payment.getCallbackStatus());
        dto.setPayTime(payment.getPayTime());
        dto.setCreateTime(payment.getCreateTime());
        return dto;
    }
}
