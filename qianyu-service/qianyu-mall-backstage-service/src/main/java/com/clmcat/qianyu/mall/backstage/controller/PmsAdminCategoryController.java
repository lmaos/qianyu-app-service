package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.*;
import com.clmcat.qianyu.mall.api.pms.PmsCategoryApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.CategoryCreateDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.CategoryTreeNodeDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.CategoryUpdateDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminCategoryDeleteDTO;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.clmcat.qianyu.mall.backstage.support.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 运营-分类管理：分类树查询（含隐藏）+ 增删改。
 * <p>CRUD 逻辑复用 {@code PmsCategoryApi}（底层 PmsCategoryManageViewBiz，含同名/子分类/关联商品校验与 level+path 自动计算）。
 */
@Tag(name = "运营-分类管理", description = "分类树/增删改")
@ApiController
@RequestMapping("/api/admin/category")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class PmsAdminCategoryController {

    @DubboReference
    private PmsCategoryApi pmsCategoryApi;

    @Operation(summary = "分类树（含隐藏分类，admin 管理用）")
    @RequiresPermission("pms:category:view")
    @PostMapping("/tree")
    public List<CategoryTreeNodeDTO> tree(@Token Long adminId) {
        return pmsCategoryApi.tree();
    }

    @Operation(summary = "创建分类")
    @RequiresPermission("pms:category:create")
    @PostMapping("/create")
    public Long create(@Token Long adminId, @Params CategoryCreateDTO dto) {
        return pmsCategoryApi.create(dto);
    }

    @Operation(summary = "更新分类")
    @RequiresPermission("pms:category:update")
    @PostMapping("/update")
    public void update(@Token Long adminId, @Params CategoryUpdateDTO dto) {
        pmsCategoryApi.update(dto);
    }

    @Operation(summary = "删除分类（逻辑删除；有子分类或关联商品会拒绝）")
    @RequiresPermission("pms:category:delete")
    @PostMapping("/delete")
    public void delete(@Token Long adminId, @Params AdminCategoryDeleteDTO dto) {
        pmsCategoryApi.delete(dto.getCategoryId());
    }
}
