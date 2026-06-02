package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "商家管理首页聚合响应")
public class MerchantDashboardVO {

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "商家审核状态: 0=待审核 1=通过 2=拒绝")
    private Integer auditStatus;

    @Schema(description = "商家状态: 0=禁用 1=启用 2=冻结")
    private Integer status;

    @Schema(description = "统计卡片列表")
    private List<StatItem> statList;

    @Getter
    @Builder
    @Schema(description = "统计卡片项")
    public static class StatItem {

        @Schema(description = "统计项 key")
        private String key;

        @Schema(description = "统计项标签")
        private String label;

        @Schema(description = "统计项值")
        private String value;
    }
}
