package com.clmcat.qianyu.mall.pms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@Table("pms_sku")
public class PmsSku {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "merchant_id", comment = "商户ID")
    private Long merchantId;

    @Column(value = "spu_id", comment = "所属SPU ID")
    private Long spuId;

    @Column(value = "sku_code", comment = "SKU编码（商户自定义或系统生成）")
    private String skuCode;

    @Column(value = "barcode", comment = "商品条形码（EAN-13/UPC-A）")
    private String barcode;

    @Column(value = "attributes", comment = "销售属性, 格式: [{\"k\":\"颜色\",\"v\":\"红色\"}]",
            typeHandler = JacksonTypeHandler.class)
    private List<LinkedHashMap<String, String>> attributes;

    @Column(value = "sku_name", comment = "SKU名称（如\"红色-XL\"），用于购物车/订单快照展示")
    private String skuName;

    @Column(value = "sku_image", comment = "SKU主图URL，NULL时回退到SPU主图")
    private String skuImage;

    @Column(value = "price", comment = "销售价格（单位: 元）")
    private BigDecimal price;

    @Column(value = "original_price", comment = "原价/划线价（单位: 元）")
    private BigDecimal originalPrice;

    @Column(value = "cost_price", comment = "成本价（单位: 元）")
    private BigDecimal costPrice;

    @Column(value = "status", comment = "状态: 0=上架, 1=下架")
    private Integer status;

    @Column(value = "is_default", comment = "是否默认SKU: 0=否, 1=是")
    private Integer isDefault;

    @Column(value = "weight", comment = "重量（千克），用于按重量计费运费模板")
    private BigDecimal weight;

    @Column(value = "volume", comment = "体积（立方米），用于按体积计费运费模板")
    private BigDecimal volume;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除, 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
