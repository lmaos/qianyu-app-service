package com.clmcat.qianyu.mall.pms.rpc;

import com.clmcat.qianyu.mall.api.pms.PmsBrandApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsBrandDto;
import com.clmcat.qianyu.mall.pms.mapper.PmsBrandMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsBrand;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsBrandTableDef.PMS_BRAND;

@DubboService
@Service
public class PmsBrandApiImpl implements PmsBrandApi {

    @Resource
    private PmsBrandMapper brandMapper;

    @Override
    public PmsBrandDto getById(Long brandId) {
        if (brandId == null || brandId <= 0) {
            return null;
        }
        PmsBrand brand = brandMapper.selectOneByQuery(
                QueryWrapper.create().where(PMS_BRAND.ID.eq(brandId))
                        .and(PMS_BRAND.DELETED.eq(0)));
        return toDto(brand);
    }

    // ==================== Internal methods for ViewBiz ====================

    public java.util.List<PmsBrand> selectByCategoryIdsOrKeyword(java.util.List<Long> categoryIds, String keyword) {
        return brandMapper.selectByCategoryIdsOrKeyword(categoryIds, keyword);
    }

    public java.util.List<PmsBrand> selectByKeyword(String keyword) {
        return brandMapper.selectByKeyword(keyword);
    }

    public PmsBrand selectOneById(Long id) {
        return brandMapper.selectOneById(id);
    }

    public int countByName(String name) {
        return brandMapper.countByName(name);
    }

    public void insertSelective(PmsBrand brand) {
        brandMapper.insertSelective(brand);
    }

    public void updateBrand(PmsBrand brand) {
        brandMapper.update(brand);
    }

    private PmsBrandDto toDto(PmsBrand entity) {
        if (entity == null) {
            return null;
        }
        PmsBrandDto dto = new PmsBrandDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setLogo(entity.getLogo());
        dto.setDescription(entity.getDescription());
        dto.setSort(entity.getSort());
        dto.setStatus(entity.getStatus());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
