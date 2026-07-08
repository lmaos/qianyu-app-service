package com.clmcat.qianyu.mall.backstage.service;

import com.clmcat.qianyu.mall.backstage.model.vo.AdminPermissionTreeNodeVO;

import java.util.List;

/**
 * 运营权限查询服务（权限树）。
 * <p>面向「运营-权限管理」管理后台页面，需 admin:permission:manage 权限。
 */
public interface AdminPermissionViewServiceBiz {

    /**
     * 查全部权限（deleted=0），按 parent_id 递归构建树；根 parent_id=0。
     */
    List<AdminPermissionTreeNodeVO> tree();
}
