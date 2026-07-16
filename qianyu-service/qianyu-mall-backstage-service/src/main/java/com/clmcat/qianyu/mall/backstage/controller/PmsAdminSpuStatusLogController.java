package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.api.pms.PmsSpuStatusLogApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuStatusLogDto;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminSpuStatusLogQueryDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 运营-商品管理：SPU 状态变更流水只读查询（审计/排查）。复用 pms:spu:view 权限码。
 */
@Tag(name = "运营-商品管理", description = "SPU 状态流水")
@ApiController
@RequestMapping("/api/admin/pms/spu-status-log")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class PmsAdminSpuStatusLogController {

    @DubboReference
    private PmsSpuStatusLogApi pmsSpuStatusLogApi;

    @Operation(summary = "状态流水分页")
    @RequiresPermission("pms:spu:view")
    @PostMapping("/page")
    public PageResultDTO<PmsSpuStatusLogDto> page(@Token Long adminId, @Params AdminSpuStatusLogQueryDTO dto) {
        return pmsSpuStatusLogApi.page(
                dto != null ? dto.getSpuId() : null,
                dto != null ? dto.getEvent() : null,
                dto != null ? dto.getSource() : null,
                dto != null ? dto.getStartTime() : null,
                dto != null ? dto.getEndTime() : null,
                dto != null && dto.getPageNum() != null ? dto.getPageNum() : 1,
                dto != null && dto.getPageSize() != null ? dto.getPageSize() : 10);
    }
}
