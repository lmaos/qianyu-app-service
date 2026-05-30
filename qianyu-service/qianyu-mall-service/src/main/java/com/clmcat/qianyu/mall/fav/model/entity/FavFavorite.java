package com.clmcat.qianyu.mall.fav.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("fav_favorite")
public class FavFavorite {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "user_id", comment = "用户ID")
    private Long userId;

    @Column(value = "target_id", comment = "目标ID（SPU ID 或店铺 ID）")
    private Long targetId;

    @Column(value = "target_type", comment = "目标类型: 1=商品(SPU) 2=店铺")
    private Integer targetType;

    @Column(value = "create_time", comment = "收藏时间（毫秒时间戳）")
    private Long createTime;
}
