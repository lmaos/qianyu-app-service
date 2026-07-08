package com.clmcat.qianyu.mall.oms.rpc;

import com.clmcat.qianyu.mall.api.oms.OmsAfterSaleApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.AftersalePageQueryDTO;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsAfterSaleDto;
import com.clmcat.qianyu.mall.oms.mapper.OmsAfterSaleMapper;
import com.clmcat.qianyu.mall.oms.model.entity.OmsAfterSale;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Service
public class OmsAfterSaleApiImpl implements OmsAfterSaleApi {

    @Resource
    private OmsAfterSaleMapper afterSaleMapper;

    @Override
    public void insert(OmsAfterSaleDto dto) {
        afterSaleMapper.insertSelective(toEntity(dto));
    }

    @Override
    public OmsAfterSaleDto findById(Long aftersaleId) {
        return toDto(afterSaleMapper.selectOneById(aftersaleId));
    }

    @Override
    public void update(OmsAfterSaleDto dto) {
        OmsAfterSale afterSale = afterSaleMapper.selectOneById(dto.getId());
        if (afterSale != null) {
            if (dto.getStatus() != null) afterSale.setStatus(dto.getStatus());
            if (dto.getRejectReason() != null) afterSale.setRejectReason(dto.getRejectReason());
            if (dto.getReturnShippingNo() != null) afterSale.setReturnShippingNo(dto.getReturnShippingNo());
            if (dto.getReturnShippingCompany() != null) afterSale.setReturnShippingCompany(dto.getReturnShippingCompany());
            if (dto.getRefundTime() != null) afterSale.setRefundTime(dto.getRefundTime());
            afterSale.setUpdateTime(System.currentTimeMillis());
            afterSaleMapper.update(afterSale);
        }
    }

    /**
     * Internal helper - get after sale entity by ID
     */
    public OmsAfterSale getAfterSaleById(Long aftersaleId) {
        return afterSaleMapper.selectOneById(aftersaleId);
    }

    /**
     * Internal helper - update after sale entity
     */
    public void updateAfterSale(OmsAfterSale afterSale) {
        afterSaleMapper.update(afterSale);
    }

    /**
     * P0-3: 售后状态机真 CAS — WHERE id + status，防并发双推进（OmsAfterSale 无 version 字段，用 status 作条件）
     * <p>已提升为 {@link OmsAfterSaleApi} 契约方法（架构红线：backstage → mall 一律走 @DubboReference）。
     * @return true 如果 CAS 成功（affected > 0）
     */
    @Override
    public boolean updateStatusCAS(Long id, int fromStatus, int toStatus, String rejectReason) {
        OmsAfterSale update = new OmsAfterSale();
        update.setStatus(toStatus);
        if (rejectReason != null) update.setRejectReason(rejectReason);
        update.setUpdateTime(System.currentTimeMillis());
        int affected = afterSaleMapper.updateByQuery(update,
                QueryWrapper.create().where("id = ?", id).and("status = ?", fromStatus));
        return affected > 0;
    }

    /**
     * 平台跨店售后分页（运营端视角）。
     * <p>逻辑删除 {@code deleted=0} 兜底过滤；merchantId/status/type 任一为 null 则跳过该条件。
     */
    @Override
    public List<OmsAfterSaleDto> pageByPlatform(AftersalePageQueryDTO query) {
        QueryWrapper qw = QueryWrapper.create().where("deleted = ?", 0);
        if (query.getMerchantId() != null) {
            qw.and("merchant_id = ?", query.getMerchantId());
        }
        if (query.getStatus() != null) {
            qw.and("status = ?", query.getStatus());
        }
        if (query.getType() != null) {
            qw.and("type = ?", query.getType());
        }
        qw.orderBy("create_time DESC");

        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;

        Page<OmsAfterSale> page = afterSaleMapper.paginate(Page.of(pageNum, pageSize), qw);
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return Collections.emptyList();
        }
        return page.getRecords().stream().map(this::toDto).collect(Collectors.toList());
    }

    public void createAfterSale(OmsAfterSale afterSale) {
        afterSaleMapper.insertSelective(afterSale);
    }

    public Page<OmsAfterSale> page(QueryWrapper qw, Page<OmsAfterSale> page) {
        return afterSaleMapper.paginate(page, qw);
    }

    private OmsAfterSaleDto toDto(OmsAfterSale entity) {
        if (entity == null) return null;
        OmsAfterSaleDto dto = new OmsAfterSaleDto();
        dto.setId(entity.getId());
        dto.setAfterSaleNo(entity.getAfterSaleNo());
        dto.setOrderId(entity.getOrderId());
        dto.setOrderItemId(entity.getOrderItemId());
        dto.setUserId(entity.getUserId());
        dto.setMerchantId(entity.getMerchantId());
        dto.setType(entity.getType());
        dto.setReason(entity.getReason());
        dto.setDescription(entity.getDescription());
        dto.setAmount(entity.getAmount());
        dto.setImages(entity.getImages());
        dto.setStatus(entity.getStatus());
        dto.setRejectReason(entity.getRejectReason());
        dto.setReturnShippingNo(entity.getReturnShippingNo());
        dto.setReturnShippingCompany(entity.getReturnShippingCompany());
        dto.setSendBackShippingNo(entity.getSendBackShippingNo());
        dto.setSendBackShippingCompany(entity.getSendBackShippingCompany());
        dto.setRefundTime(entity.getRefundTime());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    private OmsAfterSale toEntity(OmsAfterSaleDto dto) {
        OmsAfterSale entity = new OmsAfterSale();
        entity.setId(dto.getId());
        entity.setAfterSaleNo(dto.getAfterSaleNo());
        entity.setOrderId(dto.getOrderId());
        entity.setOrderItemId(dto.getOrderItemId());
        entity.setUserId(dto.getUserId());
        entity.setMerchantId(dto.getMerchantId());
        entity.setType(dto.getType());
        entity.setReason(dto.getReason());
        entity.setDescription(dto.getDescription());
        entity.setAmount(dto.getAmount());
        entity.setImages(dto.getImages());
        entity.setStatus(dto.getStatus());
        entity.setRejectReason(dto.getRejectReason());
        entity.setReturnShippingNo(dto.getReturnShippingNo());
        entity.setReturnShippingCompany(dto.getReturnShippingCompany());
        entity.setSendBackShippingNo(dto.getSendBackShippingNo());
        entity.setSendBackShippingCompany(dto.getSendBackShippingCompany());
        entity.setRefundTime(dto.getRefundTime());
        entity.setCreateTime(dto.getCreateTime());
        return entity;
    }
}
