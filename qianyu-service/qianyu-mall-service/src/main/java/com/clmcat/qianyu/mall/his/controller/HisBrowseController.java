package com.clmcat.qianyu.mall.his.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.his.model.dto.BrowseHistoryDeleteDTO;
import com.clmcat.qianyu.mall.his.model.dto.BrowseHistoryQueryDTO;
import com.clmcat.qianyu.mall.his.model.dto.BrowseRecordDTO;
import com.clmcat.qianyu.mall.his.model.vo.BrowseHistoryDeleteResultVO;
import com.clmcat.qianyu.mall.his.model.vo.BrowseHistoryItemVO;
import com.clmcat.qianyu.mall.his.service.HisBrowseViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "浏览历史", description = "商品浏览记录管理")
@ApiController
@RequestMapping("/api/mall/his")
// @LoginVerify
public class HisBrowseController {

    @Resource
    private HisBrowseViewServiceBiz browseViewServiceBiz;

    /**
     * 浏览历史列表
     */
    @Operation(summary = "浏览历史列表")
    @PostMapping("/browseHistoryList")
    public Page<BrowseHistoryItemVO> browseHistoryList(
            @Parameter(hidden = true) @Token long userId,
            @Params BrowseHistoryQueryDTO dto) {
        return browseViewServiceBiz.getBrowseHistoryList(userId, dto);
    }

    /**
     * 记录浏览
     */
    @Operation(summary = "记录浏览")
    @PostMapping("/browseRecord")
    public void browseRecord(
            @Parameter(hidden = true) @Token long userId,
            @Params BrowseRecordDTO dto) {
        browseViewServiceBiz.recordBrowse(userId, dto);
    }

    /**
     * 删除浏览历史
     */
    @Operation(summary = "删除浏览历史")
    @PostMapping("/browseHistoryDelete")
    public BrowseHistoryDeleteResultVO browseHistoryDelete(
            @Parameter(hidden = true) @Token long userId,
            @Params BrowseHistoryDeleteDTO dto) {
        return browseViewServiceBiz.deleteBrowseHistory(userId, dto);
    }
}
