package com.clmcat.qianyu.mall.api.ads.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class AdsAddressDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
}
