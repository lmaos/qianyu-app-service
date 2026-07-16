package com.clmcat.qianyu.mall.backstage.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "楼层分页查询")
public class AdminZonePageDTO {
    @Schema(description = "标题关键词")
    private String keyword;
    @Schema(description = "状态 0显示1隐藏")
    private Integer status;
    @Schema(description = "页码")
    private Integer pageNum;
    @Schema(description = "每页条数")
    private Integer pageSize;
}
