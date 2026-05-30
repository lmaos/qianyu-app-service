package com.clmcat.qianyu.mall.log.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsPushDTO;
import com.clmcat.qianyu.mall.log.service.LogisticsViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "物流回调", description = "物流公司状态推送回调")
@ApiController
@RequestMapping("/api/mall/callback/logistics")
public class LogisticsCallbackController {

    @Resource
    private LogisticsViewServiceBiz logisticsViewServiceBiz;

    /**
     * 物流公司推送 -- 物流状态变更回调
     */
    @Operation(summary = "物流公司推送")
    @PostMapping("/trackPush")
    public Boolean trackPush(@Params LogisticsPushDTO dto) {
        return logisticsViewServiceBiz.handlePush(dto);
    }
}
