package com.clmcat.qianyu.mall.inv.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "库存锁定结果")
public class StockLockResultVO {

    @Schema(description = "是否全部锁定成功")
    private Boolean success;

    @Schema(description = "锁定失败的项")
    private List<StockLockFailItemVO> failItems;
}
