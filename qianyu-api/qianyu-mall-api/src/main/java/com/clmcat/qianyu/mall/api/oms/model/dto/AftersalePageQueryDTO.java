package com.clmcat.qianyu.mall.api.oms.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 运营端售后分页查询条件（{@code OmsAfterSaleApi#pageByPlatform}）。
 * <p>跨店平台视角：所有字段均可选，传 null 表示不过滤。
 */
@Data
public class AftersalePageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商家 ID（可选过滤）。 */
    private Long merchantId;

    /** 售后状态（10/20/30/40/50/60，可选过滤）。 */
    private Integer status;

    /** 售后类型（1=仅退款 2=退货退款 3=换货 4=维修，可选过滤）。 */
    private Integer type;

    /** 创建时间起（毫秒戳，含）；空=不过滤 */
    private Long startTime;

    /** 创建时间止（毫秒戳，含）；空=不过滤 */
    private Long endTime;

    /** 买家用户 ID（精确匹配，对应 OmsAfterSale.user_id；空=不过滤） */
    private Long buyerUserId;

    /** 售后单号（模糊匹配，对应 OmsAfterSale.after_sale_no；空=不过滤） */
    private String afterSaleNo;

    /** 页码（默认 1）。 */
    private Integer pageNum;

    /** 每页大小（默认 10）。 */
    private Integer pageSize;
}
