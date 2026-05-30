package com.clmcat.qianyu.mall.his.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("his_search_keyword")
public class HisSearchKeyword {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "keyword", comment = "搜索关键词")
    private String keyword;

    @Column(value = "heat", comment = "热度（搜索次数）")
    private Integer heat;

    @Column(value = "status", comment = "状态：0=隐藏 1=正常")
    private Integer status;

    @Column(value = "create_time", comment = "首次搜索时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "最近更新时间（毫秒时间戳）")
    private Long updateTime;
}
