package com.clmcat.qianyu.mall.pms.rpc;

import com.clmcat.qianyu.mall.api.pms.PmsCategoryApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.CategoryCreateDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.CategoryTreeNodeDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.CategoryUpdateDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsCategoryDto;
import com.clmcat.qianyu.mall.pms.mapper.PmsCategoryMapper;
import com.clmcat.qianyu.mall.pms.model.dto.CategoryCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.CategoryUpdateDto;
import com.clmcat.qianyu.mall.pms.model.entity.PmsCategory;
import com.clmcat.qianyu.mall.pms.service.PmsCategoryManageViewBiz;
import com.clmcat.qianyu.mall.pms.service.impl.PmsCategoryViewBizImpl;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsCategoryTableDef.PMS_CATEGORY;

@DubboService
@Service
public class PmsCategoryApiImpl implements PmsCategoryApi {

    @Resource
    private PmsCategoryMapper categoryMapper;

    /** 复用商家侧分类 CRUD（与 userId 解耦，含同名/子分类/关联商品校验与 level+path 计算）。 */
    @Lazy
    @Resource
    private PmsCategoryManageViewBiz categoryManageViewBiz;

    /** 变更后刷新 C 端分类内存缓存；@Lazy 打破与 ViewBizImpl 的相互注入。 */
    @Lazy
    @Resource
    private PmsCategoryViewBizImpl categoryViewBiz;

    @Override
    public PmsCategoryDto getById(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            return null;
        }
        PmsCategory category = categoryMapper.selectOneByQuery(
                QueryWrapper.create().where(PMS_CATEGORY.ID.eq(categoryId))
                        .and(PMS_CATEGORY.DELETED.eq(0)));
        return toDto(category);
    }

    /**
     * 全部分类树（含隐藏分类，admin 用）。区别于 C 端 selectAllEnabled 只取 status=0。
     */
    @Override
    public List<CategoryTreeNodeDTO> tree() {
        List<PmsCategory> all = categoryMapper.selectListByQuery(
                QueryWrapper.create().where(PMS_CATEGORY.DELETED.eq(0))
                        .orderBy(PMS_CATEGORY.SORT.asc(), PMS_CATEGORY.ID.asc()));

        Map<Long, List<PmsCategory>> byParent = new LinkedHashMap<>();
        for (PmsCategory c : all) {
            Long pid = c.getParentId() != null ? c.getParentId() : 0L;
            byParent.computeIfAbsent(pid, k -> new ArrayList<>()).add(c);
        }
        return buildTree(0L, byParent);
    }

    private List<CategoryTreeNodeDTO> buildTree(Long parentId, Map<Long, List<PmsCategory>> byParent) {
        List<PmsCategory> children = byParent.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        List<CategoryTreeNodeDTO> result = new ArrayList<>();
        for (PmsCategory c : children) {
            CategoryTreeNodeDTO node = new CategoryTreeNodeDTO();
            node.setId(c.getId());
            node.setParentId(c.getParentId());
            node.setName(c.getName());
            node.setLevel(c.getLevel());
            node.setIcon(c.getIcon());
            node.setImgId(c.getImgId());
            node.setSort(c.getSort());
            node.setStatus(c.getStatus());
            node.setChildren(buildTree(c.getId(), byParent));
            result.add(node);
        }
        return result;
    }

    @Override
    public Long create(CategoryCreateDTO dto) {
        CategoryCreateDto inner = new CategoryCreateDto();
        inner.setName(dto.getName());
        inner.setParentId(dto.getParentId());
        inner.setIcon(dto.getIcon());
        inner.setImgId(dto.getImgId());
        inner.setSort(dto.getSort());
        Long id = categoryManageViewBiz.createCategory(inner);
        refreshCacheSafe();
        return id;
    }

    @Override
    public void update(CategoryUpdateDTO dto) {
        CategoryUpdateDto inner = new CategoryUpdateDto();
        inner.setCategoryId(dto.getCategoryId());
        inner.setName(dto.getName());
        inner.setParentId(dto.getParentId());
        inner.setIcon(dto.getIcon());
        inner.setSort(dto.getSort());
        inner.setEnabled(dto.getEnabled());
        categoryManageViewBiz.updateCategory(inner);
        refreshCacheSafe();
    }

    @Override
    public void delete(Long categoryId) {
        categoryManageViewBiz.deleteCategory(categoryId);
        refreshCacheSafe();
    }

    /** 刷新 C 端分类缓存；失败不阻断主流程（@Scheduled 每 30s 兜底）。 */
    private void refreshCacheSafe() {
        try {
            if (categoryViewBiz != null) {
                categoryViewBiz.refreshCategoryCache();
            }
        } catch (Exception ignored) {
            // 缓存刷新失败不影响分类增删改结果
        }
    }

    // ==================== Internal methods for ViewBiz ====================

    public List<PmsCategory> selectAllEnabled() {
        return categoryMapper.selectAllEnabled();
    }

    public PmsCategory selectOneById(Long id) {
        return categoryMapper.selectOneById(id);
    }

    public int countByNameAndParent(String name, Long parentId) {
        return categoryMapper.countByNameAndParent(name, parentId);
    }

    public void insertSelective(PmsCategory category) {
        categoryMapper.insertSelective(category);
    }

    public void updateCategory(PmsCategory category) {
        categoryMapper.update(category);
    }

    public int countChildren(Long categoryId) {
        return categoryMapper.countChildren(categoryId);
    }

    public int countProducts(Long categoryId) {
        return categoryMapper.countProducts(categoryId);
    }

    private PmsCategoryDto toDto(PmsCategory entity) {
        if (entity == null) {
            return null;
        }
        PmsCategoryDto dto = new PmsCategoryDto();
        dto.setId(entity.getId());
        dto.setParentId(entity.getParentId());
        dto.setPath(entity.getPath());
        dto.setImgId(entity.getImgId());
        dto.setName(entity.getName());
        dto.setLevel(entity.getLevel());
        dto.setIcon(entity.getIcon());
        dto.setSort(entity.getSort());
        dto.setStatus(entity.getStatus());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
