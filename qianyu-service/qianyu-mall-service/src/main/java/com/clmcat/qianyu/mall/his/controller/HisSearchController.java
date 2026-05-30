package com.clmcat.qianyu.mall.his.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.his.model.dto.SearchHotQueryDTO;
import com.clmcat.qianyu.mall.his.model.dto.SearchKeywordRecordDTO;
import com.clmcat.qianyu.mall.his.model.vo.HotKeywordVO;
import com.clmcat.qianyu.mall.his.service.HisSearchViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "搜索热词", description = "热门搜索关键词")
@ApiController
@RequestMapping("/api/mall/his")
public class HisSearchController {

    @Resource
    private HisSearchViewServiceBiz searchViewServiceBiz;

    /**
     * 搜索热词列表
     */
    @Operation(summary = "搜索热词列表")
    @PostMapping("/searchHotKeywords")
    public List<HotKeywordVO> searchHotKeywords(@Params SearchHotQueryDTO dto) {
        return searchViewServiceBiz.getHotKeywords(dto);
    }

    /**
     * 记录搜索关键词
     */
    @Operation(summary = "记录搜索关键词")
    @PostMapping("/searchKeywordRecord")
    public void searchKeywordRecord(@Params SearchKeywordRecordDTO dto) {
        searchViewServiceBiz.recordKeyword(dto);
    }
}
