package com.clmcat.qianyu.mall.pms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("pms_spu_category")
public class PmsSpuCategory {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "spu_id", comment = "SPU ID")
    private Long spuId;

    @Column(value = "category_id", comment = "分类ID")
    private Long categoryId;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;
}
