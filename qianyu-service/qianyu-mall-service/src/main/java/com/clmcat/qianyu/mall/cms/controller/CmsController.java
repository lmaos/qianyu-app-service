package com.clmcat.qianyu.mall.cms.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.qianyu.mall.cms.model.vo.HomePageVo;
import com.clmcat.qianyu.mall.cms.model.vo.TabZoneListVo;
import com.clmcat.qianyu.mall.cms.service.CmsViewBiz;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@ApiController
@RequestMapping("/api/mall/cms")
public class CmsController {

    @Resource
    private CmsViewBiz cmsViewBiz;

    // app.md §2 /api/mall/cms/homePage
    /**
     * 首页商城聚合数据
     * 一次性返回 Tab 导航 + Banner + Zone 楼层，供首页商城 Tab 初始化使用
     */
    @PostMapping("/homePage")
    public HomePageVo homePage() {
        return cmsViewBiz.getHomePage();
    }

    // app.md §3 /api/mall/cms/tabZoneList
    /**
     * 单个 Tab 下的 Zone 楼层列表（不含 Banner / Tab 导航）
     *
     * <p>切 Tab 时只刷这一段，避免重复拉 banner 和 tabList。
     *
     * <p>Req 形如：{@code POST /api/mall/cms/tabZoneList?categoryId=1389965986633728}。
     * 传 0 / null / 缺省 → 返回 recommend 默认的 zoneList（与 homePage 一致）。
     */
    @PostMapping("/tabZoneList")
    public TabZoneListVo tabZoneList(
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return cmsViewBiz.getTabZoneList(categoryId);
    }
}
