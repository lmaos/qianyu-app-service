package com.clmcat.qianyu.mall.ads.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("ads_region")
public class AdsRegion {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "parent_id", comment = "父级ID（0=顶级/省）")
    private Long parentId;

    @Column(value = "name", comment = "区域名称")
    private String name;

    @Column(value = "level", comment = "层级: 1=省 2=市 3=区/县")
    private Integer level;

    @Column(value = "code", comment = "行政区划代码")
    private String code;

    @Column(value = "sort", comment = "排序值")
    private Integer sort;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;
}
