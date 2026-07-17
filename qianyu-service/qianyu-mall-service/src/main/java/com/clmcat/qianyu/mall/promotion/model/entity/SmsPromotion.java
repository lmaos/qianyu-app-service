package com.clmcat.qianyu.mall.promotion.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("sms_promotion")
public class SmsPromotion {
    @Id(keyType = KeyType.None)
    private Long id;
    @Column("merchant_id")
    private Long merchantId;
    @Column("name")
    private String name;
    @Column("type")
    private Integer type; // 1打折 2满赠 3秒杀 4满减
    @Column("rules")
    private String rules; // JSON
    @Column("start_time")
    private Long startTime;
    @Column("end_time")
    private Long endTime;
    @Column("status")
    private Integer status; // 0禁 1启 2结束
    @Column("create_time")
    private Long createTime;
    @Column("update_time")
    private Long updateTime;
    @Column("deleted")
    private Integer deleted;
}
