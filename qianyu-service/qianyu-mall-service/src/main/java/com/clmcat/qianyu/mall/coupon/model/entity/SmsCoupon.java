package com.clmcat.qianyu.mall.coupon.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 优惠券模板（sms_coupon）。merchant_id=0 平台券，>0 商家券。
 */
@Data
@Table("sms_coupon")
public class SmsCoupon {

    @Id(keyType = KeyType.None)
    @Column(comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "merchant_id", comment = "商家ID（0=平台券，>0=商家券）")
    private Long merchantId;

    @Column(value = "name", comment = "优惠券名称")
    private String name;

    @Column(value = "type", comment = "类型：1=满减 2=折扣 3=免运费")
    private Integer type;

    @Column(value = "scope_type", comment = "适用范围：1=全平台 2=指定商家 3=指定品类 4=指定商品")
    private Integer scopeType;

    @Column(value = "scope_value", comment = "适用范围值（JSON 数组：商家/分类/SPU ID），scope_type=1 时为 null")
    private String scopeValue;

    @Column(value = "threshold", comment = "使用门槛金额（元）")
    private BigDecimal threshold;

    @Column(value = "discount_amount", comment = "优惠金额（元），满减券用")
    private BigDecimal discountAmount;

    @Column(value = "discount_rate", comment = "折扣率（8.50=85折），折扣券用")
    private BigDecimal discountRate;

    @Column(value = "total_count", comment = "发放总量")
    private Integer totalCount;

    @Column(value = "remain_count", comment = "剩余库存")
    private Integer remainCount;

    @Column(value = "per_limit", comment = "每人限领数量")
    private Integer perLimit;

    @Column(value = "start_time", comment = "生效时间（毫秒时间戳）")
    private Long startTime;

    @Column(value = "end_time", comment = "失效时间（毫秒时间戳）")
    private Long endTime;

    @Column(value = "status", comment = "状态：0=禁用 1=启用 2=已过期")
    private Integer status;

    @Column(value = "create_time", comment = "创建时间（毫秒）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除")
    private Integer deleted;
}
