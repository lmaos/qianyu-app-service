package com.clmcat.qianyu.mall.api.oms.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细 DTO（RPC 契约用）。S4 新增：供 InvStockApi.confirmStock 跨域取订单明细（skuId+quantity）核销库存。
 */
@Data
public class OmsOrderItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private Long merchantId;
    private Long spuId;
    private Long skuId;
    private String skuName;
    private String skuImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Long createTime;
}
