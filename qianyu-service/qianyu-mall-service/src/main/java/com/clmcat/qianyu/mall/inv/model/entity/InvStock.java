package com.clmcat.qianyu.mall.inv.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("inv_stock")
public class InvStock {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "sku_id", comment = "SKU ID")
    private Long skuId;

    @Column(value = "available_stock", comment = "可用库存")
    private Integer availableStock;

    @Column(value = "locked_stock", comment = "锁定库存（已下单未发货）")
    private Integer lockedStock;

    @Column(value = "safety_stock", comment = "安全库存（预警阈值）")
    private Integer safetyStock;

    @Column(value = "version", comment = "乐观锁版本号，每次更新 +1")
    private Long version;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除：0=未删除 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
