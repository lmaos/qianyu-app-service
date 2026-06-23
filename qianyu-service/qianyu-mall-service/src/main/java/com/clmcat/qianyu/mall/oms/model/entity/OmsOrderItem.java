package com.clmcat.qianyu.mall.oms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("oms_order_item")
public class OmsOrderItem {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "order_id", comment = "订单 ID")
    private Long orderId;

    @Column(value = "merchant_id", comment = "商家ID")
    private Long merchantId;

    @Column(value = "spu_id", comment = "SPU ID")
    private Long spuId;

    @Column(value = "sku_id", comment = "SKU ID")
    private Long skuId;

    @Column(value = "sku_name", comment = "SKU 名称")
    private String skuName;

    @Column(value = "sku_image", comment = "SKU 主图 URL")
    private String skuImage;

    @Column(value = "price", comment = "商品单价（元）")
    private BigDecimal price;

    @Column(value = "quantity", comment = "购买数量")
    private Integer quantity;

    @Column(value = "total_amount", comment = "行项目总金额（元）")
    private BigDecimal totalAmount;

    @Column(value = "attributes", comment = "SKU 销售属性快照JSON")
    private String attributes;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;
}
