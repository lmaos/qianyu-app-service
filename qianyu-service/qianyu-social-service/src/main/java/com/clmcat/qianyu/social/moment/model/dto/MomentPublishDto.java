package com.clmcat.qianyu.social.moment.model.dto;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentContent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "动态发布参数")
public class MomentPublishDto {
    /**
     * 动态内容
     */
    @Schema(description = "动态内容，包含文本、图片、视频等结构")
    private MomentContent content;
    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private double latitude;
    /**
     * 经度
     */
    @Schema(description = "经度")
    private double longitude;
    /**
     * 国家代码
     */
    @Schema(description = "国家代码")
    private String country;
    /**
     * 状态
     */
    @Schema(description = "动态状态")
    private Integer status;
}
