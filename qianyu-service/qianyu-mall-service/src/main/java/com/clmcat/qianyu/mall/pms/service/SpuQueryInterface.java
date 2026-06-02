package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.pms.model.dto.SpuCategorySearchDto;
import com.clmcat.qianyu.mall.pms.model.vo.SpuListItemVo;
import com.mybatisflex.core.paginate.Page;

/**
 * 商品查询接口
 * 定义分类搜索页按分类、排序、分页查询商品的契约
 */
public interface SpuQueryInterface {

    /**
     * 按分类分页查询商品
     *
     * @param dto 查询参数（categoryId / sortMode / priceDirection / pageNum / pageSize）
     * @return 商品分页列表
     */
    Page<SpuListItemVo> queryByCategory(SpuCategorySearchDto dto);
}
