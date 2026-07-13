package com.clmcat.qianyu.mall.pms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Table("pms_spu")
public class PmsSpu {

    /** 状态常量（status 字段取值）。C 端可见性只认 STATUS_ON_SALE(1)。 */
    public static final int STATUS_DRAFT = 0;         // 草稿（商户编辑态）
    public static final int STATUS_ON_SALE = 1;       // 上架（C 端可见）
    public static final int STATUS_OFF_SHELF = 2;     // 下架
    public static final int STATUS_PENDING_AUDIT = 4; // 待审核（商户提交后，等审核任务推进）
    public static final int STATUS_APPROVED = 5;      // 审核通过（待商户上架）

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "merchant_id", comment = "商户ID")
    private Long merchantId;

    @Column(value = "store_id", comment = "所属店铺ID（关联 mch_store.id）")
    private Long storeId;

    @Column(value = "brand_id", comment = "品牌ID")
    private Long brandId;

    @Column(value = "category_id", comment = "主分类ID（冗余字段，关联 pms_category.id）")
    private Long categoryId;

    @Column(value = "name", comment = "商品名称")
    private String name;

    @Column(value = "subtitle", comment = "商品副标题")
    private String subtitle;

    @Column(value = "main_image", comment = "主图URL")
    private String mainImage;

    @Column(value = "thumb_image", comment = "缩略图URL（列表页使用，避免加载原图）")
    private String thumbImage;

    @Column(value = "images", comment = "商品图片列表, 格式: [\"url1\",\"url2\"]",
            typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    @Column(value = "description", comment = "商品详情（富文本/HTML）")
    private String description;

    @Column(value = "keywords", comment = "搜索关键词（逗号分隔），用于搜索召回")
    private String keywords;

    @Column(value = "unit", comment = "计量单位（个/件/箱等）")
    private String unit;

    @Column(value = "status", comment = "状态: 0=草稿, 1=上架, 2=下架, 3=已删除, 4=待审核, 5=审核通过")
    private Integer status;

    @Column(value = "sort", comment = "排序值，越小越靠前")
    private Integer sort;

    @Column(value = "freight_template_id", comment = "运费模板ID（关联 mch_freight_template.id），NULL=免运费")
    private Long freightTemplateId;

    @Column(value = "min_price", comment = "SKU 最低价（元），冗余字段用于搜索价格区间筛选和列表展示")
    private BigDecimal minPrice;

    @Column(value = "sales", comment = "累计销量（由订单完成后异步更新）")
    private Integer sales;

    @Column(value = "comment_count", comment = "累计评价数（由评价写入时异步更新）")
    private Integer commentCount;

    @Column(value = "avg_score", comment = "平均评分（1.0~5.0），由评价写入时异步更新")
    private BigDecimal avgScore;

    @Column(value = "publish_time", comment = "最近上架时间（毫秒时间戳）")
    private Long publishTime;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除, 1=已删除", isLogicDelete = true)
    private Integer deleted;

    // ---------- JOIN 虚拟字段（非数据库列） ----------

    @Column(ignore = true)
    private String merchantName;

    @Column(ignore = true)
    private String storeName;
}
