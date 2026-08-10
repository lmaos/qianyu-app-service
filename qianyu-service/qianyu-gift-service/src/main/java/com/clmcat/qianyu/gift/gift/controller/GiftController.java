package com.clmcat.qianyu.gift.gift.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftDto;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendRequest;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendResult;
import com.clmcat.qianyu.gift.api.shelf.model.dto.ShelfConfigDto;
import com.clmcat.qianyu.gift.gift.service.GiftSendServiceBiz;
import com.clmcat.qianyu.gift.gift.service.GiftShelfServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 礼物 HTTP 接口。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@ApiController
@RequestMapping("/api/gift")
@Tag(name = "礼物", description = "送礼、礼物架、礼物查询")
public class GiftController {

    @Resource
    private GiftSendServiceBiz giftSendServiceBiz;

    @Resource
    private GiftShelfServiceBiz giftShelfServiceBiz;

    /**
     * 送礼。
     */
    @PostMapping("/send")
    @LoginVerify
    @Operation(summary = "送礼", description = "赠送礼物给主播。幂等：同 idempotentKey 重放返回已有结果")
    public GiftSendResult send(@Token long userId, @Params GiftSendRequest req) {
        req.setSenderUserId(userId);
        return giftSendServiceBiz.sendGift(req);
    }

    /**
     * 礼物详情。
     */
    @GetMapping("/detail")
    @Operation(summary = "礼物详情")
    public GiftDto detail(@RequestParam("giftId") long giftId) {
        return giftSendServiceBiz.getGift(giftId);
    }

    /**
     * 礼物架。
     */
    @GetMapping("/shelf")
    @Operation(summary = "获取礼物架", description = "按场景返回分类分组的礼物列表")
    public ShelfConfigDto shelf(@RequestParam(value = "sceneType", defaultValue = "live_room") String sceneType) {
        return giftShelfServiceBiz.getShelfConfig(sceneType);
    }
}
