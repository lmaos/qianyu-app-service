package com.clmcat.qianyu.mall.mch.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.mch.model.dto.FreightTemplateCreateDTO;
import com.clmcat.qianyu.mall.mch.model.dto.FreightTemplateIdDTO;
import com.clmcat.qianyu.mall.mch.model.dto.FreightTemplateUpdateDTO;
import com.clmcat.qianyu.mall.mch.model.vo.FreightTemplateDetailVO;
import com.clmcat.qianyu.mall.mch.model.vo.FreightTemplateVO;
import com.clmcat.qianyu.mall.mch.service.MerchantFreightViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "商家运费模板", description = "运费模板 CRUD")
@ApiController
@RequestMapping("/api/mall/merchant/mch")
// @LoginVerify
public class MerchantFreightController {

    @Resource
    private MerchantFreightViewServiceBiz freightViewServiceBiz;

    // app.md §12.2 /api/mall/merchant/mch/freightTemplateList
    /**
     * 运费模板列表
     */
    @Operation(summary = "运费模板列表")
    @PostMapping("/freightTemplateList")
    public List<FreightTemplateVO> freightTemplateList(@Parameter(hidden = true) @Token long userId) {
        return freightViewServiceBiz.getTemplateList(userId);
    }

    /**
     * 运费模板详情
     */
    @Operation(summary = "运费模板详情")
    @PostMapping("/freightTemplateDetail")
    public FreightTemplateDetailVO freightTemplateDetail(
            @Parameter(hidden = true) @Token long userId,
            @Params FreightTemplateIdDTO dto) {
        return freightViewServiceBiz.getTemplateDetail(userId, dto.getTemplateId());
    }

    /**
     * 创建运费模板
     */
    @Operation(summary = "创建运费模板")
    @PostMapping("/freightTemplateCreate")
    public Long freightTemplateCreate(
            @Parameter(hidden = true) @Token long userId,
            @Params FreightTemplateCreateDTO dto) {
        return freightViewServiceBiz.createTemplate(userId, dto);
    }

    /**
     * 更新运费模板
     */
    @Operation(summary = "更新运费模板")
    @PostMapping("/freightTemplateUpdate")
    public void freightTemplateUpdate(
            @Parameter(hidden = true) @Token long userId,
            @Params FreightTemplateUpdateDTO dto) {
        freightViewServiceBiz.updateTemplate(userId, dto);
    }

    /**
     * 删除运费模板
     */
    @Operation(summary = "删除运费模板")
    @PostMapping("/freightTemplateDelete")
    public void freightTemplateDelete(
            @Parameter(hidden = true) @Token long userId,
            @Params FreightTemplateIdDTO dto) {
        freightViewServiceBiz.deleteTemplate(userId, dto.getTemplateId());
    }
}
