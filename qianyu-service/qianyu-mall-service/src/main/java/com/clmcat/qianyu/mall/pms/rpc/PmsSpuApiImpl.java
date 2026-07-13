package com.clmcat.qianyu.mall.pms.rpc;

import com.clmcat.qianyu.mall.api.pms.PmsSpuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsSpuTableDef.PMS_SPU;

@DubboService
@Service
public class PmsSpuApiImpl implements PmsSpuApi {

    @Resource
    private PmsSpuMapper spuMapper;

    @Resource
    private com.clmcat.qianyu.mall.pms.mapper.PmsSpuCategoryMapper spuCategoryMapper;

    @Override
    public PmsSpuDto getById(Long spuId) {
        if (spuId == null || spuId <= 0) {
            return null;
        }
        PmsSpu spu = spuMapper.selectOneByQuery(
                QueryWrapper.create().where(PMS_SPU.ID.eq(spuId))
                        .and(PMS_SPU.DELETED.eq(0)));
        return toDto(spu);
    }

    @Override
    public List<PmsSpuDto> batchGetByIds(Collection<Long> spuIds) {
        if (spuIds == null || spuIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> validIds = spuIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .collect(Collectors.toList());
        if (validIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<PmsSpu> spuList = spuMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(PMS_SPU.ID.in(validIds))
                        .and(PMS_SPU.DELETED.eq(0)));
        return spuList.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void updateStatFields(Long spuId, BigDecimal minPrice, Integer sales,
                                  Integer commentCount, BigDecimal avgScore) {
        if (spuId == null || spuId <= 0) {
            return;
        }
        PmsSpu update = new PmsSpu();
        update.setId(spuId);
        if (minPrice != null) {
            update.setMinPrice(minPrice);
        }
        if (sales != null) {
            update.setSales(sales);
        }
        if (commentCount != null) {
            update.setCommentCount(commentCount);
        }
        if (avgScore != null) {
            update.setAvgScore(avgScore);
        }
        update.setUpdateTime(System.currentTimeMillis());
        spuMapper.update(update);
    }

    @Override
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<PmsSpuDto> pageByPlatform(com.clmcat.qianyu.mall.api.pms.model.dto.SpuPageQueryDTO query) {
        QueryWrapper qw = QueryWrapper.create().where(PMS_SPU.DELETED.eq(0));
        if (query.getMerchantId() != null) qw.and(PMS_SPU.MERCHANT_ID.eq(query.getMerchantId()));
        if (query.getBrandId() != null) qw.and(PMS_SPU.BRAND_ID.eq(query.getBrandId()));
        if (query.getCategoryId() != null) qw.and(PMS_SPU.CATEGORY_ID.eq(query.getCategoryId()));
        if (query.getStatus() != null) qw.and(PMS_SPU.STATUS.eq(query.getStatus()));
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            qw.and(PMS_SPU.NAME.like("%" + query.getKeyword() + "%"));
        }
        if (query.getStartTime() != null) qw.and(PMS_SPU.CREATE_TIME.ge(query.getStartTime()));
        if (query.getEndTime() != null) qw.and(PMS_SPU.CREATE_TIME.le(query.getEndTime()));
        qw.orderBy(PMS_SPU.CREATE_TIME.desc());
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;
        com.mybatisflex.core.paginate.Page<PmsSpu> page = spuMapper.paginate(
                com.mybatisflex.core.paginate.Page.of(pageNum, pageSize), qw);
        List<PmsSpuDto> records = page.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        return com.clmcat.qianyu.mall.api.model.dto.PageResultDTO.<PmsSpuDto>builder()
                .records(records).total(page.getTotalRow())
                .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
    }

    @Override
    public void adminListOff(Long spuId, String reason) {
        PmsSpu spu = spuMapper.selectOneByQuery(
                QueryWrapper.create().where(PMS_SPU.ID.eq(spuId)).and(PMS_SPU.DELETED.eq(0)));
        if (spu == null) return;
        spu.setStatus(2);
        spu.setUpdateTime(System.currentTimeMillis());
        spuMapper.update(spu);
    }

    @Override
    public void audit(Long spuId, Boolean approved, String rejectReason) {
        PmsSpu spu = spuMapper.selectOneByQuery(
                QueryWrapper.create().where(PMS_SPU.ID.eq(spuId)).and(PMS_SPU.DELETED.eq(0)));
        if (spu == null) return;
        if (Boolean.TRUE.equals(approved)) {
            spu.setStatus(1);
        }
        spu.setUpdateTime(System.currentTimeMillis());
        spuMapper.update(spu);
    }

    // ==================== Internal methods for ViewBiz ====================

    public PmsSpu selectOneById(Long id) {
        return spuMapper.selectOneById(id);
    }

    public com.mybatisflex.core.paginate.Page<PmsSpu> paginate(
            com.mybatisflex.core.paginate.Page<PmsSpu> page, com.mybatisflex.core.query.QueryWrapper qw) {
        return spuMapper.paginate(page, qw);
    }

    /**
     * SPU 分页列表（按分类/商家，仅上架状态）
     *
     * <p>实现要点：MyBatis-Flex 对 {@code @Select} 注解方法恒走 {@code selectOne}，
     * 无法承载分页结果集。改用 {@link com.mybatisflex.core.BaseMapper#paginate}
     * 配合动态 {@link QueryWrapper} 拼条件——分页 + 条件 + 排序都交给框架处理。
     *
     * <p>{@code merchantName} / {@code storeName} 不在本查询中 JOIN，由
     * {@code PmsCategoryViewBizImpl} 在转 VO 时通过 {@code PmsSupport}
     * 单独补全（避免在分页主查询里用 LEFT JOIN 拖慢大表）。
     */
    public com.mybatisflex.core.paginate.Page<PmsSpu> selectSpuList(
            com.mybatisflex.core.paginate.Page<PmsSpu> page, Long categoryId, Long merchantId) {
        QueryWrapper qw = QueryWrapper.create()
                .where(PMS_SPU.STATUS.eq(1))
                .and(PMS_SPU.DELETED.eq(0));
        if (categoryId != null) {
            qw.and(PMS_SPU.CATEGORY_ID.eq(categoryId));
        }
        if (merchantId != null) {
            qw.and(PMS_SPU.MERCHANT_ID.eq(merchantId));
        }
        qw.orderBy(PMS_SPU.SORT.asc(), PMS_SPU.CREATE_TIME.desc());
        return spuMapper.paginate(page, qw);
    }

    public void insertSelective(PmsSpu spu) {
        spuMapper.insertSelective(spu);
    }

    public void updateSpu(PmsSpu spu) {
        spuMapper.update(spu);
    }

    public void deleteSpuCategoryBySpuId(Long spuId) {
        spuCategoryMapper.deleteBySpuId(spuId);
    }

    public void batchInsertSpuCategories(java.util.List<com.clmcat.qianyu.mall.pms.model.entity.PmsSpuCategory> list) {
        spuCategoryMapper.batchInsert(list);
    }

    private PmsSpuDto toDto(PmsSpu entity) {
        if (entity == null) {
            return null;
        }
        PmsSpuDto dto = new PmsSpuDto();
        dto.setId(entity.getId());
        dto.setMerchantId(entity.getMerchantId());
        dto.setStoreId(entity.getStoreId());
        dto.setBrandId(entity.getBrandId());
        dto.setCategoryId(entity.getCategoryId());
        dto.setName(entity.getName());
        dto.setSubtitle(entity.getSubtitle());
        dto.setMainImage(entity.getMainImage());
        dto.setThumbImage(entity.getThumbImage());
        dto.setImages(entity.getImages());
        dto.setDescription(entity.getDescription());
        dto.setKeywords(entity.getKeywords());
        dto.setUnit(entity.getUnit());
        dto.setStatus(entity.getStatus());
        dto.setSort(entity.getSort());
        dto.setFreightTemplateId(entity.getFreightTemplateId());
        dto.setMinPrice(entity.getMinPrice());
        dto.setSales(entity.getSales());
        dto.setCommentCount(entity.getCommentCount());
        dto.setAvgScore(entity.getAvgScore());
        dto.setPublishTime(entity.getPublishTime());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
