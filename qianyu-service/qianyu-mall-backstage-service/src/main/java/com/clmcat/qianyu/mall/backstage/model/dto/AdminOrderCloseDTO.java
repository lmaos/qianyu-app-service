package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

/**
 * 运营端关闭订单请求。
 */
@Data
public class AdminOrderCloseDTO {
    /** 订单 ID（Snowflake） */
    private Long orderId;
}
