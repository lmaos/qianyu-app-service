package com.clmcat.qianyu.mall.cms.rpc;

import com.clmcat.qianyu.mall.api.cms.CmsHomeTabApi;
import com.clmcat.qianyu.mall.cms.mapper.CmsHomeTabMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsHomeTab;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsHomeTabTableDef.CMS_HOME_TAB;

/**
 * CMS HomeTab 管理服务（RPC）
 */
@DubboService
@Service
public class CmsHomeTabApiImpl implements CmsHomeTabApi {

    @Resource
    private CmsHomeTabMapper homeTabMapper;

    // app.md §3 /api/mall/merchant/cms/tabSetDefault
    @Override
    public void setDefault(Long tabId) {
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
    }
}
