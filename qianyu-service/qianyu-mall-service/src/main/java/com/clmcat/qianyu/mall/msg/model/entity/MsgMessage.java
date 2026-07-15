package com.clmcat.qianyu.mall.msg.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 系统通知（站内信，t=msg_message）。
 * <p>按 user_id 归属，DB 拉取式（无 Push）；is_read 标记已读。本期"商户审核结果"为首批事件，
 * type 字段可扩展订单/支付/售后/系统等。
 * <p>表结构见 mall.sql（msg_message）；本期不分片。
 */
@Data
@Table("msg_message")
public class MsgMessage {
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;
    @Column("user_id")     private Long userId;
    @Column("type")        private Integer type;        // 1=商户 2=订单 3=支付 4=售后 5=系统
    @Column("title")       private String title;
    @Column("content")     private String content;
    @Column("biz_type")    private String bizType;       // merchant_audit / order / pay ...
    @Column("biz_id")      private Long bizId;
    @Column("is_read")     private Integer isRead;       // 0=未读 1=已读
    @Column("read_time")   private Long readTime;
    @Column("create_time") private Long createTime;
    @Column("update_time") private Long updateTime;
    @Column(value = "deleted", isLogicDelete = true)
    private Integer deleted;
}
