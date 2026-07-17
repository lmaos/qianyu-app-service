package com.clmcat.qianyu.mall.inv.service.impl;

import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.pms.PmsSkuApi;
import com.clmcat.qianyu.mall.api.pms.PmsSpuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSkuDto;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto;
import com.clmcat.qianyu.mall.inv.rpc.InvStockLogApiImpl;
import com.clmcat.qianyu.mall.inv.model.dto.StockLogQueryDTO;
import com.clmcat.qianyu.mall.inv.model.entity.InvStockLog;
import com.clmcat.qianyu.mall.inv.model.entity.status.InvStatus;
import com.clmcat.qianyu.mall.inv.model.vo.StockLogItemVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.clmcat.qianyu.mall.inv.service.InvStockLogViewServiceBiz;

@Service
public class InvStockLogViewServiceBizImpl implements InvStockLogViewServiceBiz {

    @Resource
    private InvStockLogApiImpl stockLogServiceBiz;

    @DubboReference
    private MerchantApi merchantApi;

    @DubboReference
    private PmsSkuApi pmsSkuApi;

    @DubboReference
    private PmsSpuApi pmsSpuApi;

    /**
     * 查询库存日志（仅本商家 SKU；回填 spuName + typeText）
     */
    public Page<StockLogItemVO> queryLog(long userId, StockLogQueryDTO dto) {
        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        // S16: 仅查本商家 SKU 的流水（与 stockPage 一致；inv_stock_log 无 merchant_id）
        MerchantDto m = merchantApi.requireActiveMerchant(userId);
        InvStatus.INV_SKU_NOT_BELONG_MERCHANT.assertThrowResEx(m == null);
        List<PmsSkuDto> skus = pmsSkuApi.listByMerchantId(m.getId());
        if (skus == null || skus.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        Map<Long, PmsSkuDto> skuMap = new HashMap<>();
        Set<Long> spuIds = new HashSet<>();
        List<Long> merchantSkuIds = new ArrayList<>();
        for (PmsSkuDto s : skus) {
            skuMap.put(s.getId(), s);
            merchantSkuIds.add(s.getId());
            if (s.getSpuId() != null) spuIds.add(s.getSpuId());
        }
        // 批量取 SPU 名
        Map<Long, String> spuNameMap = new HashMap<>();
        if (!spuIds.isEmpty()) {
            List<PmsSpuDto> spus = pmsSpuApi.batchGetByIds(spuIds);
            if (spus != null) {
                for (PmsSpuDto spu : spus) {
                    if (spu.getId() != null) spuNameMap.put(spu.getId(), spu.getName());
                }
            }
        }

        QueryWrapper qw = QueryWrapper.create();
        qw.in("sku_id", merchantSkuIds); // 商家范围
        if (dto != null && dto.getSkuId() != null) {
            qw.and("sku_id = ?", dto.getSkuId());
        }
        qw.orderBy("create_time DESC");

        Page<InvStockLog> logPage = stockLogServiceBiz.paginate(new Page<>(pageNum, pageSize), qw);
        if (logPage == null || logPage.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        List<StockLogItemVO> voList = new ArrayList<>();
        for (InvStockLog log : logPage.getRecords()) {
            PmsSkuDto skuDto = skuMap.get(log.getSkuId());
            String spuName = (skuDto != null && skuDto.getSpuId() != null) ? spuNameMap.get(skuDto.getSpuId()) : null;
            voList.add(StockLogItemVO.builder()
                    .id(log.getId())
                    .skuId(log.getSkuId())
                    .spuName(spuName)
                    .type(log.getType())
                    .typeText(stockLogTypeText(log.getType()))
                    .quantity(log.getQuantity())
                    .beforeStock(log.getBeforeStock())
                    .afterStock(log.getAfterStock())
                    .reason(log.getRemark())
                    .createTime(String.valueOf(log.getCreateTime()))
                    .build());
        }

        Page<StockLogItemVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(logPage.getTotalRow());
        return result;
    }

    /** 库存流水类型 → 中文（对齐 StockLogQueryDTO：1下单锁定/2支付确认/3取消释放/4手动增加/5手动减少/6售后释放） */
    private String stockLogTypeText(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 1 -> "下单锁定";
            case 2 -> "支付确认";
            case 3 -> "取消释放";
            case 4 -> "手动增加";
            case 5 -> "手动减少";
            case 6 -> "售后释放";
            default -> "其他";
        };
    }
}
