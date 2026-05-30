package com.clmcat.qianyu.mall.pms.query;

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
     * @param categoryId    分类 ID（L2 或 L3，必填）
     * @param sortMode      排序方式: recommend / sales / price
     * @param priceDirection 价格排序方向: asc / desc（sortMode=price 时生效）
     * @param pageNum       页码
     * @param pageSize      每页条数
     * @return 商品分页列表
     */
    Page<SpuListItemVo> queryByCategory(Long categoryId, String sortMode, String priceDirection,
                                         int pageNum, int pageSize);
}
