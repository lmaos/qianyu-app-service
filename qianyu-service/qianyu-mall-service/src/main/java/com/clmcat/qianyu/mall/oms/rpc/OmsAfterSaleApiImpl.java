package com.clmcat.qianyu.mall.oms.rpc;

import com.clmcat.qianyu.mall.api.oms.OmsAfterSaleApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsAfterSaleDto;
import com.clmcat.qianyu.mall.oms.mapper.OmsAfterSaleMapper;
import com.clmcat.qianyu.mall.oms.model.entity.OmsAfterSale;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

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
