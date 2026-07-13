package com.clmcat.qianyu.mall.api.oms.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 运营端订单分页查询条件（跨店）。
 * <p>所有字段均可空：空表示不过滤。pageNum/pageSize 缺省由实现兜底（1/10）。
 */
@Data
public class OrderPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商家 ID（精确匹配，空=全平台） */
    private Long merchantId;

    /** 订单状态：10/20/30/40/50/60（空=全部） */
    private Integer status;

    /** 订单编号（模糊匹配） */
    private String orderNo;

    /** 买家用户 ID（精确匹配） */
    private Long buyerUserId;

    /** 支付渠道（预留，空=不过滤） */
    private Integer paymentChannel;

    /** 创建时间起（毫秒戳，含）；空=不过滤 */
    private Long startTime;

    /** 创建时间止（毫秒戳，含）；空=不过滤 */
    private Long endTime;

    /** 页码，缺省 1 */
    private Integer pageNum;

    /** 每页条数，缺省 10 */
    private Integer pageSize;
}
