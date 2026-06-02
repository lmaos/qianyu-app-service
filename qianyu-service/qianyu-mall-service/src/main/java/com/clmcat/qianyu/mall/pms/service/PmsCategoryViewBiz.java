package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.pms.model.dto.BrandListDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuListDto;
import com.clmcat.qianyu.mall.pms.model.vo.BrandVo;
import com.clmcat.qianyu.mall.pms.model.vo.CategoryPageVo;
import com.clmcat.qianyu.mall.pms.model.vo.CategoryTreeVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import com.mybatisflex.core.paginate.Page;
import java.util.List;

public interface PmsCategoryViewBiz {

    void refreshCategoryCache();

    CategoryPageVo getCategoryPage(Long categoryId);

    List<CategoryTreeVo> getCategoryTree();

    List<BrandVo> getBrandList(BrandListDto dto);

    Page<SpuSimpleVo> getSpuList(SpuListDto dto);

}