package com.clmcat.qianyu.mall.cms.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.cms.model.vo.HomePageVo;
import com.clmcat.qianyu.mall.cms.service.CmsViewBiz;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@ApiController
@RequestMapping("/api/mall/cms")
public class CmsController {

    @Resource
    private CmsViewBiz cmsViewBiz;

    /**
     * 首页商城聚合数据
     * 一次性返回 Tab 导航 + Banner + Zone 楼层，供首页商城 Tab 初始化使用
     */
    @PostMapping("/homePage")
    public HomePageVo homePage(@RequestBody(required = false) @Params Map<String, Object> params) {
        return cmsViewBiz.getHomePage();
    }
}
