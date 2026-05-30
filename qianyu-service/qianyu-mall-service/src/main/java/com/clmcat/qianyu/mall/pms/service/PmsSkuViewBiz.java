package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSku;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.entity.status.PmsStatus;
import com.clmcat.qianyu.mall.pms.model.vo.SkuItemVo;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PmsSkuViewBiz {

    @Resource
    private PmsSkuServiceBiz skuServiceBiz;

    @Resource
    private PmsSpuServiceBiz spuServiceBiz;

    @Resource
    private PmsSupport pmsSupport;

    @DubboReference
    private InvStockApi invStockApi;

    /**
     * SKU 列表
     */
    public List<SkuItemVo> getSkuList(Long spuId) {
        PmsSpu spu = spuServiceBiz.selectOneById(spuId);
        PmsStatus.PMS_SPU_NOT_FOUND.assertThrowResEx(spu == null || spu.getDeleted() == 1);

        List<PmsSku> skuList = skuServiceBiz.selectBySpuId(spuId);

        // Batch query stock for all SKUs via INV module API
        Map<Long, Integer> stockMap = new java.util.HashMap<>();
        try {
            List<Long> skuIds = skuList.stream().map(PmsSku::getId).collect(Collectors.toList());
            if (!skuIds.isEmpty()) {
                List<InvStockDto> stockInfos = invStockApi.batchQuery(skuIds);
                for (InvStockDto si : stockInfos) {
                    stockMap.put(si.getSkuId(), si.getAvailableStock());
                }
            }
        } catch (Exception e) {
            // Stock query failure should not block SKU listing
        }

        List<SkuItemVo> result = new ArrayList<>();
        for (PmsSku sku : skuList) {
            result.add(pmsSupport.toSkuItemVo(sku, spu.getMainImage(), stockMap.getOrDefault(sku.getId(), 0)));
        }
        return result;
    }
}
