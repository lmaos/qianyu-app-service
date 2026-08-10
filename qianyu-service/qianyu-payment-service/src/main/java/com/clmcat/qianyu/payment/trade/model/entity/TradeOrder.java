package com.clmcat.qianyu.payment.trade.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易订单（母表：只负责扣款侧）。
 * <p>
 * 结算明细由 {@link TradeOrderItem} 承载，一个母订单对应 1~N 条结算记录。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("trade_order")
public class TradeOrder {

    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    /** 统一交易流水号 */
    @Column("trans_no")
    private String transNo;

    /** 付款方用户ID */
    @Column("from_user_id")
    private Long fromUserId;

    /** 虚拟币消费总额（最小单位） */
    @Column("coin_amount")
    private Long coinAmount;

    /** 业务类型（gift/live_room/...） */
    @Column("biz_type")
    private String bizType;

    /** 业务单号 */
    @Column("biz_id")
    private String bizId;

    /** 幂等键 */
    @Column("idempotent_key")
    private String idempotentKey;

    /** 0=PENDING 1=SUCCESS 2=CANCELLED 3=REFUNDED */
    @Column("status")
    private Integer status;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    // ===== 状态常量 =====

    /** 0=PENDING：已创建，已冻结余额 */
    public static final int STATUS_PENDING   = 0;
    /** 1=SUCCESS：已确认，扣款+结算完成 */
    public static final int STATUS_SUCCESS   = 1;
    /** 2=CANCELLED：已取消，已解冻余额 */
    public static final int STATUS_CANCELLED = 2;
    /** 3=REFUNDED：成功后退款 */
    public static final int STATUS_REFUNDED  = 3;
}
