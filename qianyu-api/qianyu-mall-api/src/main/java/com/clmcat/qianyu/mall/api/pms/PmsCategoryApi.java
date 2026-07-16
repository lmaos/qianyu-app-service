package com.clmcat.qianyu.mall.api.pms;

import com.clmcat.qianyu.mall.api.pms.model.dto.CategoryCreateDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.CategoryTreeNodeDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.CategoryUpdateDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsCategoryDto;

import java.util.List;

public interface PmsCategoryApi {

    PmsCategoryDto getById(Long categoryId);

    /**
     * 全部分类树（含隐藏分类，admin 管理用）。
     *
     * @return 多级树结构，根节点 parentId=0
     */
    List<CategoryTreeNodeDTO> tree();

    /**
     * 创建分类（同级名称去重 / 自动算 level+path 由 service 保证）。
     *
     * @param dto 名称/父级/图标/图片/排序
     * @return 新分类 ID
     */
    Long create(CategoryCreateDTO dto);

    /**
     * 更新分类（改 parentId 时自动重算 level+path）。
     */
    void update(CategoryUpdateDTO dto);

    /**
     * 删除分类（逻辑删除）。有子分类或有关联商品时抛业务异常，由调用方捕获转信封。
     */
    void delete(Long categoryId);
}
