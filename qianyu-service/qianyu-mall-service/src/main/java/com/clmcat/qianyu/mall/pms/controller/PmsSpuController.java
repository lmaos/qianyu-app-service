package com.clmcat.qianyu.mall.pms.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.pms.model.dto.SkuIdDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuIdDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuSearchDto;
import com.clmcat.qianyu.mall.pms.model.vo.SkuItemVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuDetailVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import com.clmcat.qianyu.mall.pms.service.PmsSkuViewBiz;
import com.clmcat.qianyu.mall.pms.service.PmsSpuViewBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "C端商品搜索/详情", description = "SPU 搜索、SPU 详情、SKU 列表")
@ApiController
@RequestMapping("/api/mall/pms")
public class PmsSpuController {

    @Resource
    private PmsSpuViewBiz spuViewBiz;

    @Resource
    private PmsSkuViewBiz skuViewBiz;

    /**
     * SPU 搜索
     */
    @Operation(summary = "SPU 搜索")
    @PostMapping("/spuSearch")
    public Page<SpuSimpleVo> spuSearch(@Params SpuSearchDto dto) {
        return spuViewBiz.searchSpu(dto);
    }

    // app.md §8 /api/mall/pms/spuDetail
    /**
     * SPU 详情
     */
    @Operation(summary = "SPU 详情")
    @PostMapping("/spuDetail")
    public SpuDetailVo spuDetail(@Params SpuIdDto dto) {
        return spuViewBiz.getSpuDetail(dto.getSpuId());
    }

    /**
     * SPU 详情（带 SKU 预选）
     */
    @Operation(summary = "SPU 详情（带 SKU 预选）")
    @PostMapping("/spuDetailBySku")
    public SpuDetailVo spuDetailBySku(@Params SkuIdDto dto) {
        return spuViewBiz.getSpuDetailBySku(dto.getSkuId());
    }

    /**
     * SKU 列表
     */
    @Operation(summary = "SKU 列表")
    @PostMapping("/skuList")
    public List<SkuItemVo> skuList(@Params SpuIdDto dto) {
        return skuViewBiz.getSkuList(dto.getSpuId());
    }
}
