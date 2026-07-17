package com.clmcat.qianyu.mall.oms.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.oms.model.dto.OrderItemDTO;
import com.clmcat.qianyu.mall.oms.model.vo.FreightContext;
import com.clmcat.qianyu.mall.oms.model.vo.PriceResult;
import com.clmcat.qianyu.mall.oms.service.OrderPriceService;
import com.clmcat.qianyu.mall.api.pms.PmsSkuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSkuDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.*;

@Tag(name = "价格预览")
@ApiController
@RequestMapping("/api/mall/oms")
@LoginVerify
public class OmsPricePreviewController {

    @Resource
    private OrderPriceService priceService;
    @DubboReference
    private PmsSkuApi pmsSkuApi;

    @Operation(summary = "下单价格预览（运费+券+满减试算）")
    @PostMapping("/pricePreview")
    public PriceResult pricePreview(
            @Parameter(hidden = true) @Token long userId,
            @Params Map<String, Object> params) {
        // 解析 items
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) params.get("items");
        if (rawItems == null || rawItems.isEmpty()) return PriceResult.builder()
                .totalAmount(BigDecimal.ZERO).freightAmount(BigDecimal.ZERO)
                .couponAmount(BigDecimal.ZERO).payAmount(BigDecimal.ZERO).build();

        // 算 totalAmount + 收集 FreightContext
        Set<Long> skuIds = new HashSet<>();
        for (Map<String, Object> item : rawItems) {
            Object sid = item.get("skuId");
            if (sid != null) skuIds.add(Long.parseLong(String.valueOf(sid)));
        }
        List<PmsSkuDto> skus = pmsSkuApi.listByIds(skuIds);
        Map<Long, PmsSkuDto> skuMap = new HashMap<>();
        for (PmsSkuDto s : skus) skuMap.put(s.getId(), s);

        BigDecimal totalAmount = BigDecimal.ZERO;
        Long merchantId = null;
        List<FreightContext.FreightItem> freightItems = new ArrayList<>();
        for (Map<String, Object> item : rawItems) {
            Long skuId = Long.parseLong(String.valueOf(item.get("skuId")));
            int qty = Integer.parseInt(String.valueOf(item.getOrDefault("quantity", 1)));
            PmsSkuDto sku = skuMap.get(skuId);
            if (sku == null) continue;
            BigDecimal price = sku.getPrice() != null ? sku.getPrice() : BigDecimal.ONE;
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(qty)));
            if (merchantId == null) merchantId = sku.getMerchantId();
            freightItems.add(FreightContext.FreightItem.builder()
                    .spuId(sku.getSpuId()).quantity(qty)
                    .weight(sku.getWeight()).volume(sku.getVolume()).build());
        }

        Long couponUserId = params.get("couponUserId") != null
                ? Long.parseLong(String.valueOf(params.get("couponUserId"))) : null;

        FreightContext ctx = FreightContext.builder()
                .merchantId(merchantId)
                .province(null) // 前端预览无地址上下文，运费按全国默认
                .items(freightItems).build();

        return priceService.calculatePrice(userId, totalAmount, couponUserId, ctx);
    }
}
