package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.MerchantWithdrawalApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantPageQueryDTO;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageQueryDTO;
import com.clmcat.qianyu.mall.api.oms.OmsOrderApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OrderPageQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.vo.DashboardVO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 平台运营看板 Controller（编码运营 Phase 3·平台看板）。
 *
 * <p>类级 {@code @LoginVerify(BackstageLoginVerifyFunction) + token="X-Admin-Token"}，
 * 方法 {@code @RequiresPermission}。
 * <p>统一信封自动包装（{@code @ApiController}），返回 VO，禁手写 {@code ResponseEntity}/{@code Response<>}。
 *
 * <p>各计数查询走分页 RPC 的 {@code total} 字段（pageSize=1 即可拿到准确总数，无需拉全量记录）。
 */
@Tag(name = "运营-平台看板", description = "运营首页运营数字概览")
@ApiController
@RequestMapping("/api/admin/dashboard")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class AdminDashboardController {

    /** 看板计数查询的页大小：仅需触发分页拿到 total，无需实际记录，固定 1 条。 */
    private static final int COUNT_PAGE_SIZE = 1;

    @DubboReference
    private MerchantApi merchantApi;

    @DubboReference
    private MerchantWithdrawalApi withdrawalApi;

    @DubboReference
    private OmsOrderApi omsOrderApi;

    /**
     * 平台运营概览。
     * <p>聚合：总商户数 + 待审核商户 + 待审核提现 + 近期订单 + 待发货订单。
     * <p>权限复用运营管理权限（与商户/资金管理同域），避免为单一概览接口新增 permCode。
     *
     * @param adminId 当前运营账号 ID（@Token 注入，X-Admin-Token 解析）
     * @return 看板概览 VO
     */
    @Operation(summary = "平台运营概览")
    @RequiresPermission("admin:account:manage")
    @GetMapping("/overview")
    public DashboardVO overview(@Token Long adminId) {
        // 总商户数（auditStatus/status 任意）
        MerchantPageQueryDTO allMerchants = new MerchantPageQueryDTO();
        allMerchants.setPageNum(1);
        allMerchants.setPageSize(COUNT_PAGE_SIZE);
        long totalMerchants = merchantApi.pageMerchants(allMerchants).getTotal();

        // 待审核商户（auditStatus=0）
        MerchantPageQueryDTO pendingAudit = new MerchantPageQueryDTO();
        pendingAudit.setAuditStatus(0);
        pendingAudit.setPageNum(1);
        pendingAudit.setPageSize(COUNT_PAGE_SIZE);
        long pendingAuditMerchants = merchantApi.pageMerchants(pendingAudit).getTotal();

        // 待审核提现（status=0）
        WithdrawalPageQueryDTO pendingWithdrawal = new WithdrawalPageQueryDTO();
        pendingWithdrawal.setStatus(0);
        pendingWithdrawal.setPageNum(1);
        pendingWithdrawal.setPageSize(COUNT_PAGE_SIZE);
        long pendingWithdrawals = withdrawalApi.pageByPlatform(pendingWithdrawal).getTotal();

        // 近期订单（status 任意）
        OrderPageQueryDTO allOrders = new OrderPageQueryDTO();
        allOrders.setPageNum(1);
        allOrders.setPageSize(COUNT_PAGE_SIZE);
        long totalOrders = omsOrderApi.pageByPlatform(allOrders).getTotal();

        // 待发货订单（status=20）
        OrderPageQueryDTO pendingShip = new OrderPageQueryDTO();
        pendingShip.setStatus(20);
        pendingShip.setPageNum(1);
        pendingShip.setPageSize(COUNT_PAGE_SIZE);
        long pendingShipOrders = omsOrderApi.pageByPlatform(pendingShip).getTotal();

        return DashboardVO.builder()
                .totalMerchants(totalMerchants)
                .pendingAuditMerchants(pendingAuditMerchants)
                .pendingWithdrawals(pendingWithdrawals)
                .totalOrders(totalOrders)
                .pendingShipOrders(pendingShipOrders)
                .build();
    }
}
