package com.clmcat.qianyu.social.moment.model.dto;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentContent;
import lombok.Data;

@Data
public class MomentPublishDto {
    /**
     * 动态内容
     */
    private MomentContent content;
    /**
     * 纬度
     */
    private double latitude;
    /**
     * 经度
     */
    private double longitude;
    /**
     * 国家代码
     */
    private String country;
    /**
     * 状态
     */
    private Integer status;
}
