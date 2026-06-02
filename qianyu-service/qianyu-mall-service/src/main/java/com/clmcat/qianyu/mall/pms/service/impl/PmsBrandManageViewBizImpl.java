package com.clmcat.qianyu.mall.pms.service.impl;

import com.clmcat.qianyu.mall.pms.rpc.PmsBrandApiImpl;
import com.clmcat.qianyu.mall.pms.model.dto.BrandCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.BrandUpdateDto;
import com.clmcat.qianyu.mall.pms.model.entity.PmsBrand;
import com.clmcat.qianyu.mall.pms.model.entity.status.PmsStatus;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.clmcat.qianyu.mall.pms.service.PmsBrandManageViewBiz;

@Service
public class PmsBrandManageViewBizImpl implements PmsBrandManageViewBiz {

    @Resource
    private PmsBrandApiImpl brandServiceBiz;

    @Resource
    private PmsSupport pmsSupport;

    /**
     * 创建品牌
     */
    public Long createBrand(BrandCreateDto dto) {
        // 校验名称不重复
        int nameCount = brandServiceBiz.countByName(dto.getName());
        PmsStatus.PMS_BRAND_NAME_DUPLICATE.assertThrowResEx(nameCount > 0);

        long brandId = pmsSupport.nextId();
        long now = pmsSupport.parseTime(brandId);

        PmsBrand brand = new PmsBrand();
        brand.setId(brandId);
        brand.setName(dto.getName());
        brand.setLogo(dto.getLogo());
        brand.setDescription(dto.getDescription());
        brand.setSort(0);
        brand.setStatus(0); // 显示
        brand.setCreateTime(now);
        brand.setUpdateTime(now);
        brand.setDeleted(0);
        brandServiceBiz.insertSelective(brand);

        return brandId;
    }

    /**
     * 更新品牌
     */
    public void updateBrand(BrandUpdateDto dto) {
        PmsBrand brand = brandServiceBiz.selectOneById(dto.getBrandId());
        PmsStatus.PMS_BRAND_NOT_FOUND.assertThrowResEx(brand == null || brand.getDeleted() == 1);

        // 若修改 name，校验不重复
        if (dto.getName() != null && !dto.getName().equals(brand.getName())) {
            int nameCount = brandServiceBiz.countByName(dto.getName());
            PmsStatus.PMS_BRAND_NAME_DUPLICATE.assertThrowResEx(nameCount > 0);
        }

        long now = System.currentTimeMillis();

        PmsBrand update = new PmsBrand();
        update.setId(dto.getBrandId());

        if (dto.getName() != null) {
            update.setName(dto.getName());
        }
        if (dto.getLogo() != null) {
            update.setLogo(dto.getLogo());
        }
        if (dto.getDescription() != null) {
            update.setDescription(dto.getDescription());
        }
        if (dto.getEnabled() != null) {
            update.setStatus(dto.getEnabled() ? 0 : 1);
        }
        update.setUpdateTime(now);
        brandServiceBiz.updateBrand(update);
    }

    /**
     * 删除品牌
     */
    public void deleteBrand(Long brandId) {
        PmsBrand brand = brandServiceBiz.selectOneById(brandId);
        PmsStatus.PMS_BRAND_NOT_FOUND.assertThrowResEx(brand == null || brand.getDeleted() == 1);

        // 逻辑删除
        PmsBrand update = new PmsBrand();
        update.setId(brandId);
        update.setDeleted(1);
        update.setUpdateTime(System.currentTimeMillis());
        brandServiceBiz.updateBrand(update);
    }
}
