package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.pms.model.dto.CategoryCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.CategoryUpdateDto;
import com.clmcat.qianyu.mall.pms.model.entity.PmsCategory;
import com.clmcat.qianyu.mall.pms.model.entity.status.PmsStatus;
import com.clmcat.qianyu.mall.pms.rpc.PmsCategoryApiImpl;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PmsCategoryManageViewBiz {

    @Resource
    private PmsCategoryApiImpl categoryServiceBiz;

    @Resource
    private PmsSupport pmsSupport;

    /**
     * 创建分类
     */
    public Long createCategory(CategoryCreateDto dto) {
        Long parentId = dto.getParentId() != null ? dto.getParentId() : 0L;

        // 校验同级下名称不重复
        int nameCount = categoryServiceBiz.countByNameAndParent(dto.getName(), parentId);
        PmsStatus.PMS_CATEGORY_NAME_DUPLICATE.assertThrowResEx(nameCount > 0);

        long categoryId = pmsSupport.nextId();
        long now = pmsSupport.parseTime(categoryId);

        // 计算层级
        int level;
        String path;
        if (parentId == 0L) {
            level = 1;
            path = String.valueOf(categoryId);
        } else {
            PmsCategory parent = categoryServiceBiz.selectOneById(parentId);
            PmsStatus.PMS_CATEGORY_NOT_FOUND.assertThrowResEx(parent == null);
            level = parent.getLevel() + 1;
            path = parent.getPath() + "/" + categoryId;
        }

        PmsCategory category = new PmsCategory();
        category.setId(categoryId);
        category.setParentId(parentId);
        category.setPath(path);
        category.setImgId(dto.getImgId() != null ? dto.getImgId() : "");
        category.setName(dto.getName());
        category.setLevel(level);
        category.setIcon(dto.getIcon());
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setStatus(0); // 显示
        category.setCreateTime(now);
        category.setUpdateTime(now);
        category.setDeleted(0);
        categoryServiceBiz.insertSelective(category);

        return categoryId;
    }

    /**
     * 更新分类
     */
    public void updateCategory(CategoryUpdateDto dto) {
        PmsCategory category = categoryServiceBiz.selectOneById(dto.getCategoryId());
        PmsStatus.PMS_CATEGORY_NOT_FOUND.assertThrowResEx(category == null || category.getDeleted() == 1);

        // 若修改 name，校验同级不重复
        if (dto.getName() != null && !dto.getName().equals(category.getName())) {
            Long parentId = category.getParentId() != null ? category.getParentId() : 0L;
            int nameCount = categoryServiceBiz.countByNameAndParent(dto.getName(), parentId);
            PmsStatus.PMS_CATEGORY_NAME_DUPLICATE.assertThrowResEx(nameCount > 0);
        }

        long now = System.currentTimeMillis();

        PmsCategory update = new PmsCategory();
        update.setId(dto.getCategoryId());

        if (dto.getName() != null) {
            update.setName(dto.getName());
        }
        if (dto.getIcon() != null) {
            update.setIcon(dto.getIcon());
        }
        if (dto.getSort() != null) {
            update.setSort(dto.getSort());
        }
        if (dto.getEnabled() != null) {
            update.setStatus(dto.getEnabled() ? 0 : 1);
        }
        // 若修改 parentId，需重新计算 level 和 path
        if (dto.getParentId() != null && !dto.getParentId().equals(category.getParentId())) {
            Long newParentId = dto.getParentId();
            int newLevel;
            String newPath;
            if (newParentId == 0L) {
                newLevel = 1;
                newPath = String.valueOf(category.getId());
            } else {
                PmsCategory parent = categoryServiceBiz.selectOneById(newParentId);
                PmsStatus.PMS_CATEGORY_NOT_FOUND.assertThrowResEx(parent == null);
                newLevel = parent.getLevel() + 1;
                newPath = parent.getPath() + "/" + category.getId();
            }
            update.setParentId(newParentId);
            update.setLevel(newLevel);
            update.setPath(newPath);
        }
        update.setUpdateTime(now);
        categoryServiceBiz.updateCategory(update);
    }

    /**
     * 删除分类
     */
    public void deleteCategory(Long categoryId) {
        PmsCategory category = categoryServiceBiz.selectOneById(categoryId);
        PmsStatus.PMS_CATEGORY_NOT_FOUND.assertThrowResEx(category == null || category.getDeleted() == 1);

        // 检查子分类
        int childCount = categoryServiceBiz.countChildren(categoryId);
        PmsStatus.PMS_CATEGORY_HAS_CHILDREN.assertThrowResEx(childCount > 0);

        // 检查关联商品
        int productCount = categoryServiceBiz.countProducts(categoryId);
        PmsStatus.PMS_CATEGORY_HAS_PRODUCTS.assertThrowResEx(productCount > 0);

        // 逻辑删除
        PmsCategory update = new PmsCategory();
        update.setId(categoryId);
        update.setDeleted(1);
        update.setUpdateTime(System.currentTimeMillis());
        categoryServiceBiz.updateCategory(update);
    }
}
