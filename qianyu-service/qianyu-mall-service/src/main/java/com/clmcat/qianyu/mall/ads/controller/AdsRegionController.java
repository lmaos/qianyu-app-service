package com.clmcat.qianyu.mall.ads.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.ads.model.dto.RegionQueryDTO;
import com.clmcat.qianyu.mall.ads.model.vo.RegionNodeVO;
import com.clmcat.qianyu.mall.ads.service.AdsRegionViewBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "地区查询", description = "省市区三级联动")
@ApiController
@RequestMapping("/api/mall/ads")
public class AdsRegionController {

    @Resource
    private AdsRegionViewBiz regionViewBiz;

    @Operation(summary = "地区树查询", description = "支持懒加载，传 parentId 查子级，不传查全部省份")
    @PostMapping("/regionTree")
    public List<RegionNodeVO> regionTree(@Params RegionQueryDTO dto) {
        return regionViewBiz.getRegionList(dto);
    }
}
