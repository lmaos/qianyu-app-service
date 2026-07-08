package com.clmcat.qianyu.mall.backstage.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 平台运营看板概览 VO（GET /api/admin/dashboard/overview）。
 *
 * <p>聚合跨域关键运营数字，供运营首页看板渲染。当前 RPC 契约（{@code pageMerchants}/
 * {@code pageByPlatform} 等）返回 {@code List} 不含 total，故各计数采用「拉取较大页 + 取 list.size()」
 * 的简化策略（单次查询、避免 N+1 与新增 count RPC）。看板只需展示几个关键数字，数据可简化。
 *
 * <p>字段语义：
 * <ul>
 *   <li>{@link #totalMerchants}           平台总商户数（status 任意）</li>
 *   <li>{@link #pendingAuditMerchants}    待审核商户数（auditStatus=0）</li>
 *   <li>{@link #pendingWithdrawals}       待审核提现单数（status=0）</li>
 *   <li>{@link #totalOrders}              近期订单数（status 任意，受 pageSize 上限截断，超出时为上限值）</li>
 *   <li>{@link #pendingShipOrders}        待发货订单数（status=20，同样受上限截断）</li>
 * </ul>
 */
@Data
@Builder
public class DashboardVO {

    /** 平台总商户数（status 任意）。 */
    private long totalMerchants;

    /** 待审核商户数（auditStatus=0）。 */
    private long pendingAuditMerchants;

    /** 待审核提现单数（status=0）。 */
    private long pendingWithdrawals;

    /** 近期订单数（status 任意，受查询页大小上限截断）。 */
    private long totalOrders;

    /** 待发货订单数（status=20，受查询页大小上限截断）。 */
    private long pendingShipOrders;
}
