package com.clmcat.qianyu.mall.oms.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleCreateDTO;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleIdDTO;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleQueryDTO;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleCreateVO;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleDetailVO;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleSimpleVO;
import com.clmcat.qianyu.mall.oms.service.OmsAfterSaleViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "OMS-售后(C端)")
@ApiController
@RequestMapping("/api/mall/oms")
@LoginVerify
public class OmsAfterSaleController {

    @Resource
    private OmsAfterSaleViewServiceBiz afterSaleViewServiceBiz;

    @Operation(summary = "售后申请")
    @PostMapping("/aftersaleApply")
    public AfterSaleCreateVO aftersaleApply(
            @Parameter(hidden = true) @Token long userId,
            @Params AfterSaleCreateDTO dto) {
        return afterSaleViewServiceBiz.applyAfterSale(userId, dto);
    }

    @Operation(summary = "售后列表")
    @PostMapping("/aftersaleList")
    public Page<AfterSaleSimpleVO> aftersaleList(
            @Parameter(hidden = true) @Token long userId,
            @Params AfterSaleQueryDTO dto) {
        return afterSaleViewServiceBiz.afterSaleList(userId, dto);
    }

    @Operation(summary = "售后详情")
    @PostMapping("/aftersaleDetail")
    public AfterSaleDetailVO aftersaleDetail(
            @Parameter(hidden = true) @Token long userId,
            @Params AfterSaleIdDTO dto) {
        return afterSaleViewServiceBiz.afterSaleDetail(userId, dto.getAftersaleId());
    }

    @Operation(summary = "取消售后")
    @PostMapping("/aftersaleCancel")
    public void aftersaleCancel(
            @Parameter(hidden = true) @Token long userId,
            @Params AfterSaleIdDTO dto) {
        afterSaleViewServiceBiz.cancelAfterSale(userId, dto.getAftersaleId());
    }
}
