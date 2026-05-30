package com.clmcat.qianyu.mall.pms.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.pms.model.dto.BrandListDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuCategorySearchDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuListDto;
import com.clmcat.qianyu.mall.pms.model.vo.BrandVo;
import com.clmcat.qianyu.mall.pms.model.vo.CategoryPageVo;
import com.clmcat.qianyu.mall.pms.model.vo.CategoryTreeVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuListItemVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import com.clmcat.qianyu.mall.pms.query.SpuQueryInterface;
import com.clmcat.qianyu.mall.pms.service.PmsCategoryViewBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Tag(name = "C端商品分类/品牌", description = "分类树、品牌列表、SPU 列表~")
@ApiController
@RequestMapping("/api/mall/pms")
public class PmsCategoryController {

    @Resource
    private PmsCategoryViewBiz categoryViewBiz;

    @Resource
    private SpuQueryInterface spuQuery;

    /**
     * 分类树
     */
    @Operation(summary = "分类树")
    @PostMapping("/categoryTree")
    public List<CategoryTreeVo> categoryTree() {
        return categoryViewBiz.getCategoryTree();
    }

    /**
     * 全部分类页（v2）— 按页面结构返回 L1→L2→L3
     */
    @Operation(summary = "全部分类页数据")
    @PostMapping("/categoryPage")
    public CategoryPageVo categoryPage(@RequestBody(required = false) @Params Map<String, Object> params) {
        Long categoryId = null;
        if (params != null && params.get("categoryId") != null) {
            categoryId = Long.valueOf(params.get("categoryId").toString());
        }
        return categoryViewBiz.getCategoryPage(categoryId);
    }

    /**
     * 品牌列表
     */
    @Operation(summary = "品牌列表")
    @PostMapping("/brandList")
    public List<BrandVo> brandList(@Params BrandListDto dto) {
        return categoryViewBiz.getBrandList(dto);
    }

    /**
     * SPU 列表（按分类）
     */
    @Operation(summary = "SPU 列表")
    @PostMapping("/spuList")
    public Page<SpuSimpleVo> spuList(@Params SpuListDto dto) {
        return categoryViewBiz.getSpuList(dto);
    }

    /**
     * 分类搜索商品列表（v2）— 最小字段集 + 排序 + 分页
     */
    @Operation(summary = "分类搜索商品列表")
    @PostMapping("/categorySearch")
    public Page<SpuListItemVo> categorySearch(@RequestBody @Params SpuCategorySearchDto dto) {
        Long categoryId = dto != null ? dto.getCategoryId() : null;
        String sortMode = dto != null ? dto.getSortMode() : null;
        String priceDirection = dto != null ? dto.getPriceDirection() : null;
        int pageNum = dto != null && dto.getPageNum() != null ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null ? dto.getPageSize() : 10;

        return spuQuery.queryByCategory(categoryId, sortMode, priceDirection, pageNum, pageSize);
    }
}
