package com.clmcat.qianyu.mall.log.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("log_shipping")
public class LogShipping {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "order_id", comment = "订单ID")
    private Long orderId;

    @Column(value = "order_item_id", comment = "订单明细ID（拆物流时指定具体商品）")
    private Long orderItemId;

    @Column(value = "shipping_no", comment = "物流运单号")
    private String shippingNo;

    @Column(value = "shipping_company", comment = "物流公司编码（如 SF=顺丰、YTO=圆通）")
    private String shippingCompany;

    @Column(value = "shipping_company_name", comment = "物流公司名称")
    private String shippingCompanyName;

    @Column(value = "status", comment = "物流状态: 0=已发货 1=运输中 2=已签收 3=异常")
    private Integer status;

    @Column(value = "delivery_time", comment = "发货时间（毫秒时间戳）")
    private Long deliveryTime;

    @Column(value = "receive_time", comment = "签收时间（毫秒时间戳）")
    private Long receiveTime;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=正常 1=已删除")
    private Integer deleted;
}
