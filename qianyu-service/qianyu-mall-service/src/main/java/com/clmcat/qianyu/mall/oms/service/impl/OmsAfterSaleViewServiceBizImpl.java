package com.clmcat.qianyu.mall.oms.service.impl;

import com.clmcat.qianyu.mall.oms.rpc.OmsAfterSaleApiImpl;
import com.clmcat.qianyu.mall.oms.rpc.OmsOrderApiImpl;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleAuditDTO;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleCreateDTO;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleQueryDTO;
import com.clmcat.qianyu.mall.oms.model.entity.OmsAfterSale;
import com.clmcat.qianyu.mall.oms.model.entity.status.OmsStatus;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleCreateVO;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleDetailVO;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleSimpleVO;
import com.clmcat.qianyu.mall.oms.support.OmsSupport;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.clmcat.qianyu.mall.oms.service.OmsAfterSaleViewServiceBiz;

@Service
public class OmsAfterSaleViewServiceBizImpl implements OmsAfterSaleViewServiceBiz {

    @Resource
    private OmsAfterSaleApiImpl afterSaleServiceBiz;

    @Resource
    private OmsOrderApiImpl orderServiceBiz;

    public AfterSaleCreateVO applyAfterSale(Long userId, AfterSaleCreateDTO dto) {
        // Validate order exists and belongs to user
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(dto.getOrderId() == null);
        OmsOrderDto orderDto = orderServiceBiz.findById(dto.getOrderId());
        OmsStatus.OMS_ORDER_NOT_FOUND.assertThrowResEx(orderDto == null);
        OmsStatus.OMS_ORDER_NOT_BELONG_USER.assertThrowResEx(!orderDto.getUserId().equals(userId));
        // Only paid(20), shipped(30), completed(40) orders can apply after-sale
        OmsStatus.OMS_ORDER_STATUS_ERROR.assertThrowResEx(
                orderDto.getStatus() < 20 || orderDto.getStatus() > 40);

        Long merchantId = orderDto.getMerchantId() != null ? orderDto.getMerchantId() : 0L;

        OmsAfterSale afterSale = new OmsAfterSale();
        afterSale.setId(OmsSupport.AFTERSALE_ID_SNOWFLAKE.nextId());
        afterSale.setAfterSaleNo("AS" + afterSale.getId());
        afterSale.setUserId(userId);
        afterSale.setOrderId(dto.getOrderId());
        afterSale.setOrderItemId(dto.getOrderItemId());
        afterSale.setMerchantId(merchantId);
        afterSale.setType(dto.getType());
        afterSale.setReason(dto.getReason());
        afterSale.setDescription(dto.getDescription());
        afterSale.setStatus(OmsAfterSale.STATUS_PENDING_REVIEW);
        afterSale.setCreateTime(System.currentTimeMillis());
        afterSale.setUpdateTime(System.currentTimeMillis());
        afterSale.setDeleted(0);
        afterSaleServiceBiz.createAfterSale(afterSale);

        return AfterSaleCreateVO.builder()
                .aftersaleId(afterSale.getId())
                .aftersaleSn(afterSale.getAfterSaleNo())
                .build();
    }

    public Page<AfterSaleSimpleVO> afterSaleList(Long userId, AfterSaleQueryDTO dto) {
        int pageNum = dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create();
        qw.where("user_id = ?", userId)
                .orderBy("create_time DESC");

        Page<OmsAfterSale> page = Page.of(pageNum, pageSize);
        Page<OmsAfterSale> result = afterSaleServiceBiz.page(qw, page);

        List<AfterSaleSimpleVO> voList = new ArrayList<>();
        if (result != null && result.getRecords() != null) {
            for (OmsAfterSale as : result.getRecords()) {
                voList.add(AfterSaleSimpleVO.builder()
                        .id(as.getId())
                        .aftersaleSn("AS" + as.getId())
                        .orderId(as.getOrderId())
                        .type(as.getType())
                        .typeText(typeText(as.getType()))
                        .status(as.getStatus())
                        .statusText(statusText(as.getStatus()))
                        .createTime(String.valueOf(as.getCreateTime()))
                        .build());
            }
        }

        Page<AfterSaleSimpleVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotalRow(result != null ? result.getTotalRow() : 0);
        voPage.setPageNumber(pageNum);
        voPage.setPageSize(pageSize);
        return voPage;
    }

    public AfterSaleDetailVO afterSaleDetail(Long userId, Long aftersaleId) {
        OmsAfterSale afterSale = afterSaleServiceBiz.getAfterSaleById(aftersaleId);
        OmsStatus.OMS_AFTERSALE_NOT_FOUND.assertThrowResEx(afterSale == null);
        OmsStatus.OMS_AFTERSALE_NOT_BELONG_USER.assertThrowResEx(!afterSale.getUserId().equals(userId));

        return AfterSaleDetailVO.builder()
                .id(afterSale.getId())
                .aftersaleSn("AS" + afterSale.getId())
                .orderId(afterSale.getOrderId())
                .orderItemId(afterSale.getOrderItemId())
                .type(afterSale.getType())
                .typeText(typeText(afterSale.getType()))
                .reason(afterSale.getReason())
                .description(afterSale.getDescription())
                .status(afterSale.getStatus())
                .statusText(statusText(afterSale.getStatus()))
                .rejectReason(afterSale.getRejectReason())
                .build();
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void cancelAfterSale(Long userId, Long aftersaleId) {
        OmsAfterSale afterSale = afterSaleServiceBiz.getAfterSaleById(aftersaleId);
        OmsStatus.OMS_AFTERSALE_NOT_FOUND.assertThrowResEx(afterSale == null);
        OmsStatus.OMS_AFTERSALE_NOT_BELONG_USER.assertThrowResEx(!afterSale.getUserId().equals(userId));
        OmsStatus.OMS_AFTERSALE_STATUS_ERROR.assertThrowResEx(afterSale.getStatus() != OmsAfterSale.STATUS_PENDING_REVIEW);
        // P0-3: 真 CAS — WHERE id + status，防并发
        boolean ok = afterSaleServiceBiz.updateStatusCAS(aftersaleId, OmsAfterSale.STATUS_PENDING_REVIEW, OmsAfterSale.STATUS_CANCELLED, null);
        OmsStatus.OMS_AFTERSALE_STATUS_ERROR.assertThrowResEx(!ok);
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void auditAfterSale(Long merchantId, AfterSaleAuditDTO dto) {
        OmsAfterSale afterSale = afterSaleServiceBiz.getAfterSaleById(dto.getAftersaleId());
        OmsStatus.OMS_AFTERSALE_NOT_FOUND.assertThrowResEx(afterSale == null);
        OmsStatus.OMS_AFTERSALE_NOT_BELONG_MERCHANT.assertThrowResEx(!afterSale.getMerchantId().equals(merchantId));
        OmsStatus.OMS_AFTERSALE_STATUS_ERROR.assertThrowResEx(afterSale.getStatus() != OmsAfterSale.STATUS_PENDING_REVIEW);

        // P0-3: 真 CAS — WHERE id + status，防并发
        int toStatus = dto.getApproved() ? OmsAfterSale.STATUS_MERCHANT_AGREE : OmsAfterSale.STATUS_MERCHANT_REJECT;
        String reason = dto.getApproved() ? null : dto.getRejectReason();
        boolean ok = afterSaleServiceBiz.updateStatusCAS(dto.getAftersaleId(), OmsAfterSale.STATUS_PENDING_REVIEW, toStatus, reason);
        OmsStatus.OMS_AFTERSALE_STATUS_ERROR.assertThrowResEx(!ok);
    }

    /**
     * B端商家售后列表 — 按 merchantId 查询
     */
    public Page<AfterSaleSimpleVO> merchantAfterSaleList(Long merchantId, AfterSaleQueryDTO dto) {
        int pageNum = dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create();
        qw.where("merchant_id = ?", merchantId);
        if (dto.getStatus() != null && dto.getStatus() > 0) {
            qw.and("status = ?", dto.getStatus());
        }
        qw.orderBy("create_time DESC");

        Page<OmsAfterSale> page = Page.of(pageNum, pageSize);
        Page<OmsAfterSale> result = afterSaleServiceBiz.page(qw, page);

        List<AfterSaleSimpleVO> voList = new ArrayList<>();
        if (result != null && result.getRecords() != null) {
            for (OmsAfterSale as : result.getRecords()) {
                voList.add(AfterSaleSimpleVO.builder()
                        .id(as.getId())
                        .aftersaleSn("AS" + as.getId())
                        .orderId(as.getOrderId())
                        .type(as.getType())
                        .typeText(typeText(as.getType()))
                        .status(as.getStatus())
                        .statusText(statusText(as.getStatus()))
                        .refundAmount(as.getAmount() != null ? as.getAmount().toPlainString() : "0")
                        .createTime(String.valueOf(as.getCreateTime()))
                        .build());
            }
        }

        Page<AfterSaleSimpleVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotalRow(result != null ? result.getTotalRow() : 0);
        voPage.setPageNumber(pageNum);
        voPage.setPageSize(pageSize);
        return voPage;
    }

    private String typeText(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 1 -> "仅退款";
            case 2 -> "退货退款";
            case 3 -> "换货";
            case 4 -> "维修";
            default -> "";
        };
    }

    private String statusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 10 -> "待审核";
            case 20 -> "商家同意";
            case 30 -> "商家拒绝";
            case 40 -> "用户已发货";
            case 50 -> "已完成";
            case 60 -> "已取消";
            default -> "";
        };
    }
}
