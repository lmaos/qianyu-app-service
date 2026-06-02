package com.clmcat.qianyu.mall.pms.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.pms.model.dto.BrandCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.BrandDeleteDto;
import com.clmcat.qianyu.mall.pms.model.dto.BrandUpdateDto;
import com.clmcat.qianyu.mall.pms.model.dto.CategoryCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.CategoryDeleteDto;
import com.clmcat.qianyu.mall.pms.model.dto.CategoryUpdateDto;
import com.clmcat.qianyu.mall.pms.model.dto.MerchantGoodsQueryDTO;
import com.clmcat.qianyu.mall.pms.model.dto.SkuBatchUpdateDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuIdDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuUpdateDto;
import com.clmcat.qianyu.mall.pms.model.vo.MerchantGoodsPageVO;
import com.clmcat.qianyu.mall.pms.service.PmsBrandManageViewBiz;
import com.clmcat.qianyu.mall.pms.service.PmsCategoryManageViewBiz;
import com.clmcat.qianyu.mall.pms.service.PmsMerchantViewBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "商家商品管理", description = "SPU 创建/编辑/上下架、SKU 管理、分类/品牌 CRUD")
@ApiController
@RequestMapping("/api/mall/merchant/pms")
// @LoginVerify
public class PmsMerchantController {

    @Resource
    private PmsMerchantViewBiz merchantViewBiz;

    @Resource
    private PmsCategoryManageViewBiz categoryManageViewBiz;

    @Resource
    private PmsBrandManageViewBiz brandManageViewBiz;

    // app.md §13 /api/mall/merchant/pms/goodsPage
    @Operation(summary = "商家商品管理页")
    @PostMapping("/goodsPage")
    public MerchantGoodsPageVO goodsPage(
            @Parameter(hidden = true) @Token long userId,
            @Params MerchantGoodsQueryDTO dto) {
        return merchantViewBiz.getGoodsPage(userId, dto);
    }

    // ==================== SPU 管理 ====================

    // app.md §13.2 /api/mall/merchant/pms/spuCreate
    /**
     * 创建 SPU
     */
    @Operation(summary = "创建 SPU")
    @PostMapping("/spuCreate")
    public Long spuCreate(
            @Parameter(hidden = true) @Token long userId,
            @Params SpuCreateDto dto) {
        return merchantViewBiz.createSpu(userId, dto);
    }

    // app.md §13.2 /api/mall/merchant/pms/spuUpdate
    /**
     * 编辑 SPU
     */
    @Operation(summary = "编辑 SPU")
    @PostMapping("/spuUpdate")
    public void spuUpdate(
            @Parameter(hidden = true) @Token long userId,
            @Params SpuUpdateDto dto) {
        merchantViewBiz.updateSpu(userId, dto);
    }

    // app.md §13.2 /api/mall/merchant/pms/spuListOn
    /**
     * SPU 上架
     */
    @Operation(summary = "SPU 上架")
    @PostMapping("/spuListOn")
    public void spuListOn(
            @Parameter(hidden = true) @Token long userId,
            @Params SpuIdDto dto) {
        merchantViewBiz.listOnSpu(userId, dto.getSpuId());
    }

    // app.md §13.2 /api/mall/merchant/pms/spuListOff
    /**
     * SPU 下架
     */
    @Operation(summary = "SPU 下架")
    @PostMapping("/spuListOff")
    public void spuListOff(
            @Parameter(hidden = true) @Token long userId,
            @Params SpuIdDto dto) {
        merchantViewBiz.listOffSpu(userId, dto.getSpuId());
    }

    // app.md §13.2 /api/mall/merchant/pms/skuBatchUpdate
    /**
     * SKU 批量更新（库存/价格）
     */
    @Operation(summary = "SKU 批量更新")
    @PostMapping("/skuBatchUpdate")
    public void skuBatchUpdate(
            @Parameter(hidden = true) @Token long userId,
            @Params SkuBatchUpdateDto dto) {
        merchantViewBiz.skuBatchUpdate(userId, dto);
    }

    // ==================== 分类 CRUD ====================

    /**
     * 创建分类
     */
    @Operation(summary = "创建分类")
    @PostMapping("/categoryCreate")
    public Long categoryCreate(
            @Parameter(hidden = true) @Token long userId,
            @Params CategoryCreateDto dto) {
        return categoryManageViewBiz.createCategory(dto);
    }

    /**
     * 更新分类
     */
    @Operation(summary = "更新分类")
    @PostMapping("/categoryUpdate")
    public void categoryUpdate(
            @Parameter(hidden = true) @Token long userId,
            @Params CategoryUpdateDto dto) {
        categoryManageViewBiz.updateCategory(dto);
    }

    /**
     * 删除分类
     */
    @Operation(summary = "删除分类")
    @DeleteMapping("/categoryDelete")
    public void categoryDelete(
            @Parameter(hidden = true) @Token long userId,
            @Params CategoryDeleteDto dto) {
        categoryManageViewBiz.deleteCategory(dto.getCategoryId());
    }

    // ==================== 品牌 CRUD ====================

    /**
     * 创建品牌
     */
    @Operation(summary = "创建品牌")
    @PostMapping("/brandCreate")
    public Long brandCreate(
            @Parameter(hidden = true) @Token long userId,
            @Params BrandCreateDto dto) {
        return brandManageViewBiz.createBrand(dto);
    }

    /**
     * 更新品牌
     */
    @Operation(summary = "更新品牌")
    @PostMapping("/brandUpdate")
    public void brandUpdate(
            @Parameter(hidden = true) @Token long userId,
            @Params BrandUpdateDto dto) {
        brandManageViewBiz.updateBrand(dto);
    }

    /**
     * 删除品牌
     */
    @Operation(summary = "删除品牌")
    @DeleteMapping("/brandDelete")
    public void brandDelete(
            @Parameter(hidden = true) @Token long userId,
            @Params BrandDeleteDto dto) {
        brandManageViewBiz.deleteBrand(dto.getBrandId());
    }
}
