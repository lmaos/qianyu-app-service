package com.clmcat.qianyu.mall.rev.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("rev_review_stat")
public class RevReviewStat {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "spu_id", comment = "SPU ID")
    private Long spuId;

    @Column(value = "sku_id", comment = "SKU ID（0=SPU 汇总，非零=具体 SKU 统计）")
    private Long skuId;

    @Column(value = "total_count", comment = "总评价数")
    private Integer totalCount;

    @Column(value = "good_count", comment = "好评数（score >= 4）")
    private Integer goodCount;

    @Column(value = "mid_count", comment = "中评数（score = 3）")
    private Integer midCount;

    @Column(value = "bad_count", comment = "差评数（score <= 2）")
    private Integer badCount;

    @Column(value = "image_count", comment = "带图评价数")
    private Integer imageCount;

    @Column(value = "avg_score", comment = "平均评分（1.0~5.0）")
    private BigDecimal avgScore;

    @Column(value = "good_rate", comment = "好评率（百分比，如 98.50 表示 98.50%）")
    private BigDecimal goodRate;

    @Column(value = "update_time", comment = "最后更新时间（毫秒时间戳）")
    private Long updateTime;
}
