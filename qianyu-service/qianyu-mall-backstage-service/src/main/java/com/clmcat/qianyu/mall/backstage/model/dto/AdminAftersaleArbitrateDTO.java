package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 运营售后仲裁（介入）请求（{@code /api/admin/aftersale/arbitrate}）。
 * <p>{@code approved=true} → {@code updateStatusCAS(id, fromStatus, 20, null)} 同意；
 * {@code approved=false} → {@code updateStatusCAS(id, fromStatus, 30, rejectReason)} 驳回。
 * {@code fromStatus} 由服务端根据当前售后单 status 自动获取，前端无需传。
 */
@Data
public class AdminAftersaleArbitrateDTO {

    /** 售后单 ID。 */
    private Long aftersaleId;

    /** true=同意（→20 商家同意态），false=驳回（→30 商家拒绝态）。 */
    private Boolean approved;

    /** 驳回原因（approved=false 时必填）。 */
    private String rejectReason;
}
