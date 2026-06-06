package com.clmcat.qianyu.mall.inv.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.inv.model.dto.StockAdjustDTO;
import com.clmcat.qianyu.mall.inv.model.dto.StockLogQueryDTO;
import com.clmcat.qianyu.mall.inv.model.dto.StockPageQueryDTO;
import com.clmcat.qianyu.mall.inv.model.vo.StockAdjustResultVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockLogItemVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockPageItemVO;
import com.clmcat.qianyu.mall.inv.service.InvStockLogViewServiceBiz;
import com.clmcat.qianyu.mall.inv.service.InvStockViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "商家库存管理", description = "商家库存调整与日志查询")
@ApiController
@RequestMapping("/api/mall/merchant/inv")
// @LoginVerify
public class InvMerchantController {

    @Resource
    private InvStockViewServiceBiz stockViewServiceBiz;

    @Resource
    private InvStockLogViewServiceBiz stockLogViewServiceBiz;

    @Operation(summary = "库存调整")
    @PostMapping("/stockAdjust")
    public StockAdjustResultVO stockAdjust(
            @Parameter(hidden = true) @Token long userId,
            @Params StockAdjustDTO dto) {
        return stockViewServiceBiz.adjustStock(userId, dto);
    }

    @Operation(summary = "库存日志")
    @PostMapping("/stockLog")
    public Page<StockLogItemVO> stockLog(
            @Parameter(hidden = true) @Token long userId,
            @Params StockLogQueryDTO dto) {
        return stockLogViewServiceBiz.queryLog(userId, dto);
    }

    @Operation(summary = "库存分页查询")
    @PostMapping("/stockPage")
    public Page<StockPageItemVO> stockPage(
            @Parameter(hidden = true) @Token long userId,
            @Params StockPageQueryDTO dto) {
        return stockViewServiceBiz.stockPage(userId, dto);
    }
}
