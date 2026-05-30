package com.clmcat.qianyu.mall.oms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("oms_cart")
public class OmsCart {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "user_id", comment = "用户 ID")
    private Long userId;

    @Column(value = "merchant_id", comment = "商家ID（用于购物车按店铺分组）")
    private Long merchantId;

    @Column(value = "spu_id", comment = "SPU ID")
    private Long spuId;

    @Column(value = "sku_id", comment = "SKU ID")
    private Long skuId;

    @Column(value = "sku_name", comment = "SKU名称快照")
    private String skuName;

    @Column(value = "sku_image", comment = "SKU主图URL快照")
    private String skuImage;

    @Column(value = "quantity", comment = "商品数量")
    private Integer quantity;

    @Column(value = "checked", comment = "勾选状态: 0=未勾选, 1=已勾选")
    private Integer checked;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除, 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
