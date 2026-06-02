package com.clmcat.qianyu.mall.pms.service.impl;

import com.clmcat.qianyu.mall.pms.rpc.PmsBrandApiImpl;
import com.clmcat.qianyu.mall.pms.rpc.PmsCategoryApiImpl;
import com.clmcat.qianyu.mall.pms.rpc.PmsSpuApiImpl;
import com.clmcat.qianyu.mall.pms.model.dto.BrandListDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuListDto;
import com.clmcat.qianyu.mall.pms.model.entity.PmsBrand;
import com.clmcat.qianyu.mall.pms.model.entity.PmsCategory;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.vo.BrandVo;
import com.clmcat.qianyu.mall.pms.model.vo.CategoryFirstVo;
import com.clmcat.qianyu.mall.pms.model.vo.CategoryPageVo;
import com.clmcat.qianyu.mall.pms.model.vo.CategorySecondVo;
import com.clmcat.qianyu.mall.pms.model.vo.CategoryThirdVo;
import com.clmcat.qianyu.mall.pms.model.vo.CategoryTreeVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.clmcat.qianyu.mall.pms.service.PmsCategoryViewBiz;

@Slf4j
@Service
public class PmsCategoryViewBizImpl implements PmsCategoryViewBiz {

    @Resource
    private PmsCategoryApiImpl categoryServiceBiz;

    @Resource
    private PmsBrandApiImpl brandServiceBiz;

    @Resource
    private PmsSpuApiImpl spuServiceBiz;

    @Resource
    private PmsSupport pmsSupport;

    // ==================== 内存缓存（categoryPage v2）====================

    /** 预构建的 L1 列表（不可变，定时刷新时整体替换） */
    private volatile List<CategoryFirstVo> cachedFirstCategoryList = new ArrayList<>();

    /** categoryId → L1 ID 的映射（用于 initialFirstCategoryId 解析） */
    private volatile Map<Long, Long> cachedCategoryToL1Map = new HashMap<>();

    /**
     * 每分钟的第 0 秒和第 30 秒刷新分类缓存
     */
    @Scheduled(cron = "0,30 * * * * *")
    public void refreshCategoryCache() {
        try {
            List<PmsCategory> allCategories = categoryServiceBiz.selectAllEnabled();

            // 按 parentId 分组
            Map<Long, List<PmsCategory>> groupByParent = new LinkedHashMap<>();
            for (PmsCategory cat : allCategories) {
                groupByParent.computeIfAbsent(cat.getParentId(), k -> new ArrayList<>()).add(cat);
            }

            // 构建 L1 → L2 → L3
            List<PmsCategory> l1List = groupByParent.getOrDefault(0L, new ArrayList<>());
            List<CategoryFirstVo> firstList = new ArrayList<>();
            Map<Long, Long> categoryToL1 = new HashMap<>();

            for (PmsCategory l1 : l1List) {
                categoryToL1.put(l1.getId(), l1.getId());

                List<PmsCategory> l2List = groupByParent.getOrDefault(l1.getId(), new ArrayList<>());
                List<CategorySecondVo> secondList = new ArrayList<>();
                for (PmsCategory l2 : l2List) {
                    categoryToL1.put(l2.getId(), l1.getId());

                    List<PmsCategory> l3List = groupByParent.getOrDefault(l2.getId(), new ArrayList<>());
                    List<CategoryThirdVo> thirdList = new ArrayList<>();
                    for (PmsCategory l3 : l3List) {
                        categoryToL1.put(l3.getId(), l1.getId());
                        thirdList.add(CategoryThirdVo.builder()
                                .id(l3.getId())
                                .name(l3.getName())
                                .imageUrl(l3.getIcon())
                                .build());
                    }
                    secondList.add(CategorySecondVo.builder()
                            .id(l2.getId())
                            .name(l2.getName())
                            .thirdCategoryList(thirdList)
                            .build());
                }
                firstList.add(CategoryFirstVo.builder()
                        .id(l1.getId())
                        .name(l1.getName())
                        .secondCategoryList(secondList)
                        .build());
            }

            // 整体替换引用（volatile 保证可见性）
            this.cachedFirstCategoryList = firstList;
            this.cachedCategoryToL1Map = categoryToL1;

            log.debug("分类缓存刷新完成: L1={}, 总映射={}", firstList.size(), categoryToL1.size());
        } catch (Exception e) {
            log.error("分类缓存刷新失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 全部分类页数据（v2：从内存缓存读取）
     */
    public CategoryPageVo getCategoryPage(Long categoryId) {
        List<CategoryFirstVo> firstList = this.cachedFirstCategoryList;
        Map<Long, Long> catToL1 = this.cachedCategoryToL1Map;

        // 缓存为空时触发一次同步加载（首次启动保护）
        if (firstList.isEmpty()) {
            refreshCategoryCache();
            firstList = this.cachedFirstCategoryList;
            catToL1 = this.cachedCategoryToL1Map;
        }

        // 解析 initialFirstCategoryId
        Long initialId = null;
        if (!firstList.isEmpty()) {
            if (categoryId != null && catToL1.containsKey(categoryId)) {
                initialId = catToL1.get(categoryId);
            } else {
                initialId = firstList.get(0).getId();
            }
        }

        return CategoryPageVo.builder()
                .firstCategoryList(firstList)
                .initialFirstCategoryId(initialId)
                .build();
    }

    /**
     * 分类树（v1）
     */
    public List<CategoryTreeVo> getCategoryTree() {
        List<PmsCategory> allCategories = categoryServiceBiz.selectAllEnabled();

        // 按 parentId 分组
        Map<Long, List<PmsCategory>> groupByParent = new LinkedHashMap<>();
        for (PmsCategory cat : allCategories) {
            groupByParent.computeIfAbsent(cat.getParentId(), k -> new ArrayList<>()).add(cat);
        }

        // 递归构建树（从顶级 parentId=0 开始）
        return buildTree(0L, groupByParent);
    }

    private List<CategoryTreeVo> buildTree(Long parentId, Map<Long, List<PmsCategory>> groupByParent) {
        List<PmsCategory> children = groupByParent.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }

        List<CategoryTreeVo> result = new ArrayList<>();
        for (PmsCategory cat : children) {
            Long imgId = null;
            if (cat.getImgId() != null) {
                try {
                    imgId = Long.parseLong(cat.getImgId());
                } catch (NumberFormatException ignored) {
                    // img_id 是 VARCHAR，无法转为 long 时保留 null
                }
            }

            CategoryTreeVo node = CategoryTreeVo.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .icon(cat.getIcon())
                    .imgId(imgId)
                    .sort(cat.getSort())
                    .children(buildTree(cat.getId(), groupByParent))
                    .build();
            result.add(node);
        }
        return result;
    }

    /**
     * 品牌列表
     */
    public List<BrandVo> getBrandList(BrandListDto dto) {
        List<PmsBrand> brands;

        if (dto != null && dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            brands = brandServiceBiz.selectByCategoryIdsOrKeyword(dto.getCategoryIds(),
                    dto != null ? dto.getKeyword() : null);
        } else {
            brands = brandServiceBiz.selectByKeyword(dto != null ? dto.getKeyword() : null);
        }

        List<BrandVo> result = new ArrayList<>();
        for (PmsBrand brand : brands) {
            result.add(BrandVo.builder()
                    .id(brand.getId())
                    .name(brand.getName())
                    .logo(brand.getLogo())
                    .description(brand.getDescription())
                    .build());
        }
        return result;
    }

    /**
     * SPU 列表（按分类/商家）
     */
    public Page<SpuSimpleVo> getSpuList(SpuListDto dto) {
        int pageNum = dto != null && dto.getPageNum() != null ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null ? dto.getPageSize() : 10;

        Page<PmsSpu> page = new Page<>(pageNum, pageSize);
        Page<PmsSpu> spuPage = spuServiceBiz.selectSpuList(page,
                dto != null ? dto.getCategoryId() : null,
                dto != null ? dto.getMerchantId() : null);

        if (spuPage == null) {
            return new Page<>(pageNum, pageSize);
        }
        return spuPage.map(pmsSupport::toSpuSimpleVo);
    }
}
