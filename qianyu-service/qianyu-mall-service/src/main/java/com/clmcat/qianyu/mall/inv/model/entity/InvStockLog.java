package com.clmcat.qianyu.mall.inv.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("inv_stock_log")
public class InvStockLog {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "sku_id", comment = "SKU ID")
    private Long skuId;

    @Column(value = "order_id", comment = "关联订单 ID")
    private Long orderId;

    @Column(value = "type", comment = "变更类型：1=商家调整 2=下单锁定 3=取消释放 4=支付确认 5=售后释放")
    private Integer type;

    @Column(value = "quantity", comment = "变更数量（正数增加，负数减少）")
    private Integer quantity;

    @Column(value = "before_stock", comment = "变更前可用库存")
    private Integer beforeStock;

    @Column(value = "after_stock", comment = "变更后可用库存")
    private Integer afterStock;

    @Column(value = "remark", comment = "备注")
    private String remark;

    @Column(value = "archived", comment = "归档标记: 0=在线 1=已归档")
    private Integer archived;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;
}
