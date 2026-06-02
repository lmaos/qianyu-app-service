package com.clmcat.qianyu.mall.log.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsQueryDTO;
import com.clmcat.qianyu.mall.log.model.vo.LogisticsDetailVO;
import com.clmcat.qianyu.mall.log.service.LogisticsViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "物流查询", description = "C 端物流信息查询")
@ApiController
@RequestMapping("/api/mall/log")
// @LoginVerify
public class LogisticsController {

    @Resource
    private LogisticsViewServiceBiz logisticsViewServiceBiz;

    /**
     * 物流查询 -- 根据订单 ID 查询物流信息
     */
    @Operation(summary = "物流查询")
    @PostMapping("/logisticsQuery")
    public LogisticsDetailVO logisticsQuery(
            @Parameter(hidden = true) @Token long userId,
            @Params LogisticsQueryDTO dto) {
        return logisticsViewServiceBiz.queryByOrderId(userId, dto);
    }

    /**
     * 物流轨迹（实时） -- 从物流公司查询最新轨迹
     */
    @Operation(summary = "物流轨迹（实时）")
    @PostMapping("/logisticsTrack")
    public LogisticsDetailVO logisticsTrack(
            @Parameter(hidden = true) @Token long userId,
            @Params LogisticsQueryDTO dto) {
        return logisticsViewServiceBiz.trackRealtime(userId, dto);
    }
}
