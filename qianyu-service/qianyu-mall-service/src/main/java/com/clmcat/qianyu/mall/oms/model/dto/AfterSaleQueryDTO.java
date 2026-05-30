package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "售后列表查询请求")
public class AfterSaleQueryDTO {

    @Schema(description = "状态筛选：0=全部, 10=待审核, 20=商家同意, 30=商家拒绝, 40=用户已发货, 50=已完成, 60=已取消")
    private Integer status = 0;

    @Schema(description = "页码，默认 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，默认 10")
    private Integer pageSize = 10;
}
