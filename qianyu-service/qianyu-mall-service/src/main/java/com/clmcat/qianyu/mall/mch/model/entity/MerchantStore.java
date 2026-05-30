package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("mch_store")
public class MerchantStore {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "merchant_id", comment = "所属商家ID")
    private Long merchantId;

    @Column(value = "name", comment = "店铺名称")
    private String name;

    @Column(value = "contact_phone", comment = "店铺联系电话")
    private String contactPhone;

    @Column(value = "logo", comment = "店铺Logo URL")
    private String logo;

    @Column(value = "cover_image", comment = "店铺封面图 URL")
    private String coverImage;

    @Column(value = "description", comment = "店铺简介")
    private String description;

    @Column(value = "status", comment = "店铺状态: 0=关闭 1=正常 2=装修中")
    private Integer status;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=正常 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
