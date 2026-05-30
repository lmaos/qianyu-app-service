package com.clmcat.qianyu.mall.rev.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

@Data
@Table("rev_review")
public class RevReview {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "order_id", comment = "订单ID")
    private Long orderId;

    @Column(value = "order_item_id", comment = "订单明细ID")
    private Long orderItemId;

    @Column(value = "user_id", comment = "评价用户ID")
    private Long userId;

    @Column(value = "spu_id", comment = "商品SPU ID")
    private Long spuId;

    @Column(value = "sku_id", comment = "商品SKU ID")
    private Long skuId;

    @Column(value = "sku_name", comment = "SKU名称快照（评价时锁定的规格名称）")
    private String skuName;

    @Column(value = "merchant_id", comment = "商家ID")
    private Long merchantId;

    @Column(value = "score", comment = "评分: 1~5 分")
    private Integer score;

    @Column(value = "content", comment = "评价内容")
    private String content;

    @Column(value = "images", comment = "评价图片列表, 格式: [\"url1\",\"url2\"]",
            typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    @Column(value = "is_anonymous", comment = "是否匿名: 0=否 1=是")
    private Integer isAnonymous;

    @Column(value = "status", comment = "评价状态: 0=隐藏 1=正常 2=违规")
    private Integer status;

    @Column(value = "reply_content", comment = "商家回复内容")
    private String replyContent;

    @Column(value = "reply_time", comment = "商家回复时间（毫秒时间戳）")
    private Long replyTime;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=正常 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
