package com.clmcat.qianyu.mall.oms.model.dto;

import com.clmcat.framework.webmvc.anns.Params;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建订单请求")
public class OrderCreateDTO {

    @Schema(description = "收货地址 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long addressId;

    @Schema(description = "订单商品项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemDTO> items;

    @Schema(description = "用户优惠券 ID")
    private Long couponUserId;

    @Schema(description = "订单备注")
    private String remark;

    @Params(scope = Params.ParamsScope.IP)
    @Schema(description = "下单客户端IP", hidden = true)
    private String buyerIp;

    @Schema(description = "客户端幂等令牌(防重复提交)")
    private String clientToken;
}
