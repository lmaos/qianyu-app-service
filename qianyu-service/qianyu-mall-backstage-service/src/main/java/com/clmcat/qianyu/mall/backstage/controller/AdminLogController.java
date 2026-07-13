package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminLogQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminLoginLog;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminOpLog;
import com.clmcat.qianyu.mall.backstage.service.AdminLogViewServiceBiz;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营-日志查询 Controller（操作日志 + 登录日志，审计只增）。
 * <p>类级 {@code @LoginVerify(BackstageLoginVerifyFunction.class, token="X-Admin-Token")}；
 * 方法 {@code @RequiresPermission("admin:oplog:view")}。
 */
@Tag(name = "运营-日志查询", description = "操作日志/登录日志")
@ApiController
@RequestMapping("/api/admin")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class AdminLogController {

    @Resource
    private AdminLogViewServiceBiz logService;

    @Operation(summary = "操作日志分页（按 admin_id/perm_code/时间范围 过滤）")
    @RequiresPermission("admin:oplog:view")
    @PostMapping("/oplog/page")
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<AdminOpLog> oplogPage(
            @Token Long adminId, @Params AdminLogQueryDTO dto) {
        return logService.pageOpLog(dto);
    }

    @Operation(summary = "登录日志分页（按 admin_id/时间范围 过滤）")
    @RequiresPermission("admin:oplog:view")
    @PostMapping("/loginlog/page")
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<AdminLoginLog> loginlogPage(
            @Token Long adminId, @Params AdminLogQueryDTO dto) {
        return logService.pageLoginLog(dto);
    }
}
