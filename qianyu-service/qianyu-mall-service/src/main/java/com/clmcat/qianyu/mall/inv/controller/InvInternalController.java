package com.clmcat.qianyu.mall.inv.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.inv.model.dto.StockBatchQueryDTO;
import com.clmcat.qianyu.mall.inv.model.dto.StockConfirmDTO;
import com.clmcat.qianyu.mall.inv.model.dto.StockLockDTO;
import com.clmcat.qianyu.mall.inv.model.dto.StockReleaseDTO;
import com.clmcat.qianyu.mall.inv.model.vo.StockInfoVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockLockResultVO;
import com.clmcat.qianyu.mall.inv.service.InvStockViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "库存内部接口", description = "供 OMS/PMS 内部调用的库存 RPC")
@ApiController
@LoginVerify
@RequestMapping("/api/mall/internal/inv")
public class InvInternalController {

    @Resource
    private InvStockViewServiceBiz stockViewServiceBiz;

    @Operation(summary = "锁定库存（下单时调用）")
    @PostMapping("/stockLock")
    public StockLockResultVO stockLock(@Params StockLockDTO dto) {
        return stockViewServiceBiz.lockStock(dto);
    }

    @Operation(summary = "确认库存（支付成功后调用）")
    @PostMapping("/stockConfirm")
    public void stockConfirm(@Params StockConfirmDTO dto) {
        stockViewServiceBiz.confirmStock(dto);
    }

    @Operation(summary = "释放库存（订单取消/支付超时/售后完成）")
    @PostMapping("/stockRelease")
    public void stockRelease(@Params StockReleaseDTO dto) {
        stockViewServiceBiz.releaseStock(dto);
    }

    @Operation(summary = "批量查询库存")
    @PostMapping("/stockBatchQuery")
    public List<StockInfoVO> stockBatchQuery(@Params StockBatchQueryDTO dto) {
        return stockViewServiceBiz.batchQuery(dto);
    }
}
