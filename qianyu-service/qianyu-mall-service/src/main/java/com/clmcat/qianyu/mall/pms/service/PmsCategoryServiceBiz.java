package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.api.pms.PmsCategoryApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsCategoryDto;
import com.clmcat.qianyu.mall.pms.mapper.PmsCategoryMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsCategory;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsCategoryTableDef.PMS_CATEGORY;

@DubboService
@Service
public class PmsCategoryServiceBiz implements PmsCategoryApi {

    @Resource
    private PmsCategoryMapper categoryMapper;

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

    // ==================== Internal methods for ViewBiz ====================

    public java.util.List<PmsCategory> selectAllEnabled() {
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
