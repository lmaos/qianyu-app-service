package com.clmcat.qianyu.mall.ads.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("ads_address")
public class AdsAddress {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "user_id", comment = "用户ID")
    private Long userId;

    @Column(value = "name", comment = "收货人姓名")
    private String name;

    @Column(value = "phone", comment = "收货人手机号")
    private String phone;

    @Column(value = "country", comment = "国家代码")
    private String country;

    @Column(value = "province", comment = "省")
    private String province;

    @Column(value = "city", comment = "市")
    private String city;

    @Column(value = "district", comment = "区/县")
    private String district;

    @Column(value = "detail", comment = "详细地址")
    private String detail;

    @Column(value = "province_code", comment = "省编码")
    private String provinceCode;

    @Column(value = "city_code", comment = "市编码")
    private String cityCode;

    @Column(value = "district_code", comment = "区编码")
    private String districtCode;

    @Column(value = "is_default", comment = "是否默认: 0=否 1=是")
    private Integer isDefault;

    @Column(value = "tag", comment = "地址标签")
    private String tag;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=正常 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
