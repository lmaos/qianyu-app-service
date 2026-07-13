package com.clmcat.qianyu.mall.log.controller;

import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsCreateDTO;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsListQueryDTO;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsUpdateDTO;
import com.clmcat.qianyu.mall.log.model.vo.LogisticsListItemVO;
import com.clmcat.qianyu.mall.log.service.LogisticsViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "商家物流管理", description = "商家发货物流操作")
@ApiController
@RequestMapping("/api/mall/merchant/log")
@LoginVerify
public class LogisticsMerchantController {

    @Resource
    private LogisticsViewServiceBiz logisticsViewServiceBiz;

    /**
     * 创建物流单 -- 发货时创建物流记录
     */
    @Operation(summary = "创建物流单")
    @PostMapping("/logisticsCreate")
    public Long logisticsCreate(
            @Parameter(hidden = true) @Token long userId,
            @Params LogisticsCreateDTO dto) {
        return logisticsViewServiceBiz.createLogistics(userId, dto);
    }

    /**
     * 更新物流 -- 修改物流信息
     */
    @Operation(summary = "更新物流")
    @PostMapping("/logisticsUpdate")
    public void logisticsUpdate(
            @Parameter(hidden = true) @Token long userId,
            @Params LogisticsUpdateDTO dto) {
        logisticsViewServiceBiz.updateLogistics(userId, dto);
    }

    /**
     * 物流列表 — 商家查询所有物流单
     */
    @Operation(summary = "物流列表")
    @PostMapping("/logisticsList")
    public Page<LogisticsListItemVO> logisticsList(
            @Parameter(hidden = true) @Token long userId,
            @Params LogisticsListQueryDTO dto) {
        return logisticsViewServiceBiz.logisticsList(userId, dto);
    }
}
