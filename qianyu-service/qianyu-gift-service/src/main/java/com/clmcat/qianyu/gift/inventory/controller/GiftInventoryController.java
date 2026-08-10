package com.clmcat.qianyu.gift.inventory.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.gift.api.inventory.model.dto.InventoryListDto;
import com.clmcat.qianyu.gift.api.prop.model.dto.PropItemDto;
import com.clmcat.qianyu.gift.inventory.service.InventoryServiceBiz;
import com.clmcat.qianyu.gift.inventory.service.PropServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 背包 & 道具 HTTP 接口。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@ApiController
@RequestMapping("/api/gift/inventory")
@LoginVerify
@Tag(name = "背包道具", description = "礼物背包、道具穿戴/卸下/使用")
public class GiftInventoryController {

    @Resource
    private InventoryServiceBiz inventoryServiceBiz;

    @Resource
    private PropServiceBiz propServiceBiz;

    /**
     * 背包礼物列表。
     */
    @GetMapping("/items")
    @Operation(summary = "背包礼物列表")
    public InventoryListDto items(@Token long userId,
                                  @RequestParam(value = "cursor", defaultValue = "0") long cursor,
                                  @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return inventoryServiceBiz.getGiftInventory(userId, cursor, limit);
    }

    /**
     * 道具背包列表。
     */
    @GetMapping("/props")
    @Operation(summary = "道具背包列表")
    public List<PropItemDto> props(@Token long userId) {
        return propServiceBiz.getPropInventory(userId);
    }

    /**
     * 穿戴道具。
     */
    @PostMapping("/equip")
    @Operation(summary = "穿戴道具", description = "同类型已穿戴的会被自动卸下")
    public void equip(@Token long userId, @RequestParam("propRecordId") long propRecordId) {
        propServiceBiz.wearProp(userId, propRecordId);
    }

    /**
     * 卸下道具。
     */
    @PostMapping("/unequip")
    @Operation(summary = "卸下道具")
    public void unequip(@Token long userId, @RequestParam("propType") String propType) {
        propServiceBiz.removeProp(userId, propType);
    }

    /**
     * 使用消耗型道具。
     */
    @PostMapping("/use")
    @Operation(summary = "使用道具", description = "使用消耗型道具（消耗即销毁）")
    public void use(@Token long userId, @RequestParam("propRecordId") long propRecordId) {
        propServiceBiz.useProp(userId, propRecordId);
    }

    /**
     * 当前穿戴的道具。
     */
    @GetMapping("/equipped")
    @Operation(summary = "当前穿戴的道具")
    public Map<String, PropItemDto> equipped(@Token long userId) {
        return propServiceBiz.getEquippedProps(userId);
    }
}
