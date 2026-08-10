package com.clmcat.qianyu.gift.gift.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 送礼记录表。
 * <p>
 * 每笔送礼对应一条记录。冗余快照字段防止礼物配置变更后历史数据失真。
 * {@code tradeId} 关联 trade_order，形成完整财务对账链路。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("gift_send_record")
public class GiftSendRecord {

    /** 送礼记录ID（雪花） */
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    /** 消费流水号（来自扣款侧，一次扣款多条记录相同） */
    @Column("trans_no")
    private String transNo;

    /** 业务流水号（唯一，单次送=trans_no，批量送=独立子编号） */
    @Column("biz_no")
    private String bizNo;

    /** 送礼人用户ID */
    @Column("sender_user_id")
    private Long senderUserId;

    /** 收礼人用户ID（主播） */
    @Column("receiver_user_id")
    private Long receiverUserId;

    /** 礼物ID */
    @Column("gift_id")
    private Long giftId;

    /** 礼物名称（快照） */
    @Column("gift_name")
    private String giftName;

    /** 礼物单价（快照） */
    @Column("gift_price")
    private Long giftPrice;

    /** 赠送数量 */
    @Column("quantity")
    private Integer quantity;

    /** 总金额 */
    @Column("total_amount")
    private Long totalAmount;

    /** 实际结算礼物ID（盲盒场景） */
    @Column("actual_gift_id")
    private Long actualGiftId;

    /** 实际结算礼物名称 */
    @Column("actual_gift_name")
    private String actualGiftName;

    /** 场景：live_room/voice_room/private_chat */
    @Column("scene_type")
    private String sceneType;

    /** 直播间ID */
    @Column("room_id")
    private Long roomId;

    /** 支付方式：1=虚拟币 2=背包礼物 */
    @Column("pay_type")
    private Integer payType;

    /** 幂等键 */
    @Column("idempotent_key")
    private String idempotentKey;

    /** 分佣比例快照（万分比） */
    @Column("commission_rate")
    private Integer commissionRate;

    /** 主播实际结算金额 */
    @Column("settle_amount")
    private Long settleAmount;

    /** 1=成功 2=已退款 */
    @Column("status")
    private Integer status;

    /** 备注 */
    @Column("remark")
    private String remark;

    /** 送礼时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    // ===== 常量 =====
    public static final int PAY_TYPE_COIN     = 1;
    public static final int PAY_TYPE_BACKPACK = 2;

    public static final int STATUS_SUCCESS  = 1;
    public static final int STATUS_REFUNDED = 2;

    public static final String SCENE_LIVE_ROOM    = "live_room";
    public static final String SCENE_VOICE_ROOM   = "voice_room";
    public static final String SCENE_PRIVATE_CHAT = "private_chat";
}
