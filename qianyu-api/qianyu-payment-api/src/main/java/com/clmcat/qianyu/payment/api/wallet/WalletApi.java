package com.clmcat.qianyu.payment.api.wallet;

import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionListDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.WalletDto;

/**
 * 虚拟货币钱包 RPC API。
 * <p>
 * 提供收入、支出、查询余额和交易流水的能力。
 * 由 {@code qianyu-payment-service} 中的 {@code WalletServiceBiz} 通过 Dubbo 暴露。
 * <p>
 * 容错设计：
 * <ul>
 *   <li>所有资金操作均需传入 {@code idempotentKey}，基于唯一索引防重</li>
 *   <li>支出操作使用原子 SQL（{@code WHERE balance >= amount}）防透支</li>
 *   <li>余额变更与流水记录在同一事务内，保证一致性</li>
 * </ul>
 *
 * @author ark-home
 * @date 2026-08-03
 */
public interface WalletApi {

    /**
     * 收入（加钱）。无余额校验，直接原子增加。
     *
     * @param userId        用户ID
     * @param amount        金额（最小单位，必须 &gt; 0）
     * @param bizType       业务类型（如 gift、live_room、admin）
     * @param bizId         业务单号（关联业务记录的ID）
     * @param idempotentKey 幂等键（全局唯一，用于防重）
     * @return 交易流水
     */
    TransactionDto credit(long userId, long amount, String bizType, String bizId, String idempotentKey);

    /**
     * 支出（扣钱）。使用原子 SQL 校验余额，不足时抛出 {@code R_ACCOUNT_LESS_MONEY}。
     *
     * @param userId        用户ID
     * @param amount        金额（最小单位，必须 &gt; 0）
     * @param bizType       业务类型（如 gift、live_room）
     * @param bizId         业务单号
     * @param idempotentKey 幂等键（全局唯一，用于防重）
     * @return 交易流水
     */
    TransactionDto deduct(long userId, long amount, String bizType, String bizId, String idempotentKey);

    /**
     * 查询用户钱包余额。
     *
     * @param userId 用户ID
     * @return 钱包信息，不存在返回 null
     */
    WalletDto getWallet(long userId);

    /**
     * 退款。将一笔支出的金额退回用户钱包。
     * <p>
     * 操作内容：
     * <ol>
     *   <li>校验原流水：必须存在、属于本用户、支出类型、状态为成功</li>
     *   <li>标记原流水 status=2（已回退）</li>
     *   <li>原子加回余额 + 插入退款流水</li>
     * </ol>
     *
     * @param userId           用户ID
     * @param originalTransId  原支出流水的 id
     * @param idempotentKey    退款幂等键
     * @return 退款流水
     */
    TransactionDto refund(long userId, long originalTransId, String idempotentKey);

    /**
     * 查询用户交易流水（游标分页，按 id 倒序）。
     *
     * @param userId 用户ID
     * @param cursor 上一页最后一条的 id，首次传 0
     * @param limit  每页条数
     * @return 交易流水列表
     */
    TransactionListDto getTransactions(long userId, long cursor, int limit);
}
