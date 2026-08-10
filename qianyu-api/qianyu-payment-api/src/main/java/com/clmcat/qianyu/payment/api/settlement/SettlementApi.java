package com.clmcat.qianyu.payment.api.settlement;

import com.clmcat.qianyu.payment.api.settlement.model.dto.SettlementDto;
import com.clmcat.qianyu.payment.api.settlement.model.dto.SettlementRecordDto;
import com.clmcat.qianyu.payment.api.settlement.model.dto.SettlementRecordListDto;

/**
 * 主播结算货币 RPC API。
 * <p>
 * 与虚拟币（{@code WalletApi}）隔离，结算货币仅通过交易订单（{@code trade_order}）产生，
 * 不可直接消费，不可转账，仅供提现参考。
 *
 * @author ark-home
 * @date 2026-08-03
 */
public interface SettlementApi {

    /**
     * 结算收入（仅内部调用，不对前端暴露）。
     *
     * @param userId        主播用户ID
     * @param amount        结算金额
     * @param settleType    收入类型（1=礼物收益 2=直播收益 3=活动奖励 4=退款）
     * @param transNo       消费流水号
     * @param bizNo         业务流水号（唯一，单次=transNo，批量=独立子编号）
     * @param commissionRate 当时分佣比例（万分比）
     * @param idempotentKey 幂等键
     * @return 结算流水
     */
    SettlementRecordDto credit(long userId, long amount, int settleType, String transNo, String bizNo,
                               int commissionRate, String idempotentKey);

    /**
     * 查询主播结算账户。
     *
     * @param userId 主播用户ID
     * @return 结算账户信息，不存在返回 null
     */
    SettlementDto getSettlement(long userId);

    /**
     * 查询结算流水（游标分页，按 id 倒序）。
     *
     * @param userId 主播用户ID
     * @param cursor 上一页最后一条的 id，首次传 0
     * @param limit  每页条数
     * @return 结算流水列表
     */
    SettlementRecordListDto getRecords(long userId, long cursor, int limit);
}
