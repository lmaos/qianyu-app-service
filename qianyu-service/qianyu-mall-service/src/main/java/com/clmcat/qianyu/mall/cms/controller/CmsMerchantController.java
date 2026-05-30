package com.clmcat.qianyu.mall.cms.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.mall.cms.model.entity.CmsHomeTab;
import com.clmcat.qianyu.mall.cms.mapper.CmsHomeTabMapper;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsHomeTabTableDef.CMS_HOME_TAB;

@ApiController
@RequestMapping("/api/mall/merchant/cms")
public class CmsMerchantController {

    @Resource
    private CmsHomeTabMapper homeTabMapper;

    /**
     * 设置默认 Tab
     * 将指定 Tab 设为默认，取消其他 Tab 的默认状态
     */
    @PostMapping("/tabSetDefault")
    public String setDefaultTab(@RequestBody @Params Map<String, Object> params) {
        Long tabId = Long.valueOf(params.get("tabId").toString());

        // 1. 取消所有 Tab 的默认
        List<CmsHomeTab> allTabs = homeTabMapper.selectListByQuery(
                QueryWrapper.create().where(CMS_HOME_TAB.IS_DEFAULT.eq(1))
        );
        for (CmsHomeTab tab : allTabs) {
            CmsHomeTab update = new CmsHomeTab();
            update.setId(tab.getId());
            update.setIsDefault(0);
            homeTabMapper.update(update);
        }

        // 2. 设置指定 Tab 为默认
        CmsHomeTab update = new CmsHomeTab();
        update.setId(tabId);
        update.setIsDefault(1);
        homeTabMapper.update(update);

        return "OK";
    }
}
