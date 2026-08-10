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
 * 交易订单子表（结算明细）。
 * <p>
 * 一个 {@link TradeOrder} 对应 1~N 条结算记录。
 * 单送：1 条，bizNo = transNo；批量送：N 条，各自独立 bizNo。
 *
 * @author ark-home
 * @date 2026-08-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("trade_order_item")
public class TradeOrderItem {

    /** 子项ID（雪花） */
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    /** 关联 trade_order.id */
    @Column("order_id")
    private Long orderId;

    /** 消费流水号（冗余，避免 JOIN） */
    @Column("trans_no")
    private String transNo;

    /** 业务流水号（唯一） */
    @Column("biz_no")
    private String bizNo;

    /** 付款方用户ID（冗余，避免 JOIN 查"我的送礼"） */
    @Column("from_user_id")
    private Long fromUserId;

    /** 收款方用户ID（主播） */
    @Column("to_user_id")
    private Long toUserId;

    /** 结算货币金额（最小单位） */
    @Column("settle_amount")
    private Long settleAmount;

    /** 分佣比例（万分比） */
    @Column("commission_rate")
    private Integer commissionRate;

    /** 0=PENDING 1=SUCCESS 2=CANCELLED */
    @Column("status")
    private Integer status;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    // ===== 状态常量 =====

    public static final int STATUS_PENDING   = 0;
    public static final int STATUS_SUCCESS   = 1;
    public static final int STATUS_CANCELLED = 2;
}
