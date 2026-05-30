package com.clmcat.qianyu.mall.fav.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.fav.model.dto.FavBatchCancelDTO;
import com.clmcat.qianyu.mall.fav.model.dto.FavBatchStatusDTO;
import com.clmcat.qianyu.mall.fav.model.dto.FavListQueryDTO;
import com.clmcat.qianyu.mall.fav.model.dto.FavTargetDTO;
import com.clmcat.qianyu.mall.fav.model.vo.*;
import com.clmcat.qianyu.mall.fav.service.FavViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "收藏", description = "商品/店铺收藏管理")
@ApiController
@RequestMapping("/api/mall/fav")
// @LoginVerify
public class FavController {

    @Resource
    private FavViewServiceBiz favViewServiceBiz;

    /**
     * 添加收藏
     */
    @Operation(summary = "添加收藏")
    @PostMapping("/favAdd")
    public FavActionResultVO favAdd(
            @Parameter(hidden = true) @Token long userId,
            @Params FavTargetDTO dto) {
        return favViewServiceBiz.addFav(userId, dto);
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @PostMapping("/favCancel")
    public FavActionResultVO favCancel(
            @Parameter(hidden = true) @Token long userId,
            @Params FavTargetDTO dto) {
        return favViewServiceBiz.cancelFav(userId, dto);
    }

    /**
     * 收藏列表
     */
    @Operation(summary = "收藏列表")
    @PostMapping("/favList")
    public Page<FavItemVO> favList(
            @Parameter(hidden = true) @Token long userId,
            @Params FavListQueryDTO dto) {
        return favViewServiceBiz.getFavList(userId, dto);
    }

    /**
     * 收藏状态查询
     */
    @Operation(summary = "收藏状态查询")
    @PostMapping("/favStatus")
    public FavStatusVO favStatus(
            @Parameter(hidden = true) @Token long userId,
            @Params FavTargetDTO dto) {
        return favViewServiceBiz.getFavStatus(userId, dto);
    }

    /**
     * 批量收藏状态查询
     */
    @Operation(summary = "批量收藏状态查询")
    @PostMapping("/favBatchStatus")
    public FavBatchStatusResultVO favBatchStatus(
            @Parameter(hidden = true) @Token long userId,
            @Params FavBatchStatusDTO dto) {
        return favViewServiceBiz.getBatchFavStatus(userId, dto);
    }

    /**
     * 批量取消收藏
     */
    @Operation(summary = "批量取消收藏")
    @PostMapping("/favBatchCancel")
    public FavBatchCancelResultVO favBatchCancel(
            @Parameter(hidden = true) @Token long userId,
            @Params FavBatchCancelDTO dto) {
        return favViewServiceBiz.batchCancelFav(userId, dto);
    }
}
