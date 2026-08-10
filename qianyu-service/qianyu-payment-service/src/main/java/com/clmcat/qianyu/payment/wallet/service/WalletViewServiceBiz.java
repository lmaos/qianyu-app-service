package com.clmcat.qianyu.payment.wallet.service;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionListDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.WalletDto;
import com.clmcat.qianyu.payment.wallet.model.dto.TransactionQueryDto;
import com.clmcat.qianyu.payment.wallet.model.dto.WalletOperateDto;
import com.clmcat.qianyu.payment.wallet.model.vo.TransactionListVo;
import com.clmcat.qianyu.payment.wallet.model.vo.TransactionVo;
import com.clmcat.qianyu.payment.wallet.model.vo.WalletVo;
import com.clmcat.qianyu.payment.wallet.support.WalletSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 钱包视图/聚合服务。
 * <p>
 * 供 Controller 层调用，负责参数校验、VO 组装。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Service
public class WalletViewServiceBiz {

    @Resource
    private WalletServiceBiz walletServiceBiz;

    /** 收入 */
    public TransactionVo credit(long userId, WalletOperateDto dto) {
        validateOperateDto(dto);
        TransactionDto result = walletServiceBiz.credit(userId, dto.getAmount(),
                dto.getBizType(), dto.getBizId(), dto.getIdempotentKey());
        return toVo(result);
    }

    /** 支出 */
    public TransactionVo deduct(long userId, WalletOperateDto dto) {
        validateOperateDto(dto);
        TransactionDto result = walletServiceBiz.deduct(userId, dto.getAmount(),
                dto.getBizType(), dto.getBizId(), dto.getIdempotentKey());
        return toVo(result);
    }

    /** 查询余额 */
    public WalletVo getWallet(long userId) {
        WalletDto dto = walletServiceBiz.getWallet(userId);
        return toVo(dto);
    }

    /** 交易流水 */
    public TransactionListVo getTransactions(TransactionQueryDto query) {
        long userId = query != null && query.getUserId() != null ? query.getUserId() : 0L;
        long cursor = query != null && query.getCursor() != null ? query.getCursor() : 0L;
        int limit = query != null && query.getLimit() != null ? query.getLimit() : 20;
        TransactionListDto dto = walletServiceBiz.getTransactions(userId, cursor, limit);
        return toListVo(dto);
    }

    // ---- 校验 ----

    private void validateOperateDto(WalletOperateDto dto) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto == null);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto.getAmount() == null || dto.getAmount() <= 0, "金额必须大于0");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto.getBizType() == null || dto.getBizType().trim().isEmpty(), "业务类型不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto.getIdempotentKey() == null || dto.getIdempotentKey().trim().isEmpty(), "幂等键不能为空");
    }

    // ---- 转换 ----

    /** WalletDto → WalletVo */
    public static WalletVo toVo(WalletDto dto) {
        if (dto == null) {
            return null;
        }
        return WalletVo.builder()
                .userId(dto.getUserId())
                .balance(dto.getBalance())
                .totalIncome(dto.getTotalIncome())
                .totalExpense(dto.getTotalExpense())
                .build();
    }

    /** TransactionDto → TransactionVo */
    public static TransactionVo toVo(TransactionDto dto) {
        if (dto == null) {
            return null;
        }
        return TransactionVo.builder()
                .id(dto.getId())
                .transNo(dto.getTransNo())
                .userId(dto.getUserId())
                .transType(dto.getTransType())
                .amount(dto.getAmount())
                .balanceBefore(dto.getBalanceBefore())
                .balanceAfter(dto.getBalanceAfter())
                .bizType(dto.getBizType())
                .bizId(dto.getBizId())
                .status(dto.getStatus())
                .createTime(dto.getCreateTime())
                .build();
    }

    /** TransactionListDto → TransactionListVo */
    public static TransactionListVo toListVo(TransactionListDto dto) {
        if (dto == null || dto.getTransactions() == null || dto.getTransactions().isEmpty()) {
            return TransactionListVo.EMPTY;
        }
        List<TransactionVo> vos = dto.getTransactions().stream()
                .map(WalletViewServiceBiz::toVo)
                .collect(Collectors.toList());
        return TransactionListVo.builder()
                .transactions(vos)
                .cursor(dto.getCursor())
                .hasMore(dto.getHasMore())
                .build();
    }
}
