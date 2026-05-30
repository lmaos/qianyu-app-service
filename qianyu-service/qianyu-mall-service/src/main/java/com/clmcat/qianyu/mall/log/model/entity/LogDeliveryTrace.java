package com.clmcat.qianyu.mall.log.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("log_delivery_trace")
public class LogDeliveryTrace {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "shipping_id", comment = "物流单ID（关联 log_shipping.id）")
    private Long shippingId;

    @Column(value = "trace_time", comment = "轨迹发生时间（毫秒时间戳）")
    private Long traceTime;

    @Column(value = "description", comment = "轨迹描述")
    private String description;

    @Column(value = "location", comment = "所在城市/地区")
    private String location;

    @Column(value = "source", comment = "查询来源: 1=第三方回调推送 2=主动查询第三方 3=用户手动触发查询")
    private Integer source;

    @Column(value = "carrier_code", comment = "物流公司编码")
    private String carrierCode;

    @Column(value = "raw_data", comment = "第三方返回的原始数据，用于对账和排错")
    private String rawData;

    @Column(value = "create_time", comment = "记录创建时间（毫秒时间戳）")
    private Long createTime;
}
