package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.rev.RevReviewApi;
import com.clmcat.qianyu.mall.api.rev.model.dto.RevReviewDto;
import com.clmcat.qianyu.mall.api.rev.model.dto.ReviewPageQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminReviewBatchUpdateDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营评价管理 Controller。
 * <p>类级 @LoginVerify(BackstageLoginVerifyFunction) + 方法 @RequiresPermission。
 * <p>跨店分页查询 + 批量改状态。架构红线：admin → mall 一律走 Dubbo RPC（@DubboReference）。
 *
 * <p>评价状态字典（与 qianyu-mall-service 的 RevReview 实体常量一致）：
 * <pre>
 *   0 = STATUS_HIDDEN   隐藏
 *   1 = STATUS_NORMAL   正常
 *   2 = STATUS_VIOLATION 违规
 * </pre>
 */
@Tag(name = "运营-评价管理", description = "跨店分页/批量改状态")
@ApiController
@RequestMapping("/api/admin/review")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class RevAdminReviewController {

    @DubboReference
    private RevReviewApi revReviewApi;

    /**
     * 评价列表分页（跨店）。
     * <p>所有状态评价均可见（含 0=隐藏 / 2=违规），便于运营审计；
     * 按 spuId/merchantId/status/score 过滤，create_time DESC 排序。
     *
     * @param adminId 当前运营账号 ID（@Token 注入）
     * @param query   分页与过滤条件
     * @return 当前页 RevReviewDto 列表（框架自动包装统一信封）
     */
    @Operation(summary = "评价列表分页（跨店）")
    @RequiresPermission("rev:review:view")
    @PostMapping("/page")
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<RevReviewDto> page(
            @Token Long adminId, @Params ReviewPageQueryDTO query) {
        return revReviewApi.pageByPlatform(query);
    }

    /**
     * 批量修改评价状态。
     * <p>典型场景：批量隐藏违评 / 批量恢复 / 批量标违规。
     * 调 {@link RevReviewApi#batchUpdateStatus}，其底层按 id 逐条 update 并包在事务内；
     * ids 为空或 status 非法时由实现层抛 P_NOTNULL / P_VALUE_ERROR。
     *
     * @param adminId 当前运营账号 ID（@Token 注入，用于操作审计）
     * @param dto     批量请求（ids + status）
     */
    @Operation(summary = "批量修改评价状态")
    @RequiresPermission("rev:review:batchUpdate")
    @PostMapping("/batchUpdateStatus")
    public void batchUpdateStatus(@Token Long adminId, @Params AdminReviewBatchUpdateDTO dto) {
        revReviewApi.batchUpdateStatus(dto.getIds(), dto.getStatus());
    }
}
