package com.clmcat.qianyu.mall.his.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("his_browse_history")
public class HisBrowseHistory {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "user_id", comment = "用户ID")
    private Long userId;

    @Column(value = "spu_id", comment = "商品SPU ID")
    private Long spuId;

    @Column(value = "spu_name", comment = "商品名称快照")
    private String spuName;

    @Column(value = "spu_image", comment = "商品主图URL快照")
    private String spuImage;

    @Column(value = "price", comment = "浏览时的最低SKU价格（元）")
    private BigDecimal price;

    @Column(value = "browse_time", comment = "浏览时间（毫秒时间戳）")
    private Long browseTime;
}
