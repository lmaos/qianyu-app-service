package com.clmcat.qianyu.mall.backstage.service;

import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminLogQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminLoginLog;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminOpLog;

/**
 * 运营日志查询服务（操作日志 + 登录日志，审计只增）。
 * <p>面向「运营-日志查询」管理后台页面，需 admin:oplog:view 权限。
 */
public interface AdminLogViewServiceBiz {

    /**
     * 操作日志分页：按 admin_id / perm_code / 时间范围 过滤，按 ts DESC。
     * @return 分页结果（records 为 AdminOpLog，含 total/页码）
     */
    PageResultDTO<AdminOpLog> pageOpLog(AdminLogQueryDTO dto);

    /**
     * 登录日志分页：按 admin_id / 时间范围 过滤，按 login_at DESC。
     * <p>perm_code 维度不适用（登录日志无该字段），dto.permCode 忽略。
     * @return 分页结果（records 为 AdminLoginLog，含 total/页码）
     */
    PageResultDTO<AdminLoginLog> pageLoginLog(AdminLogQueryDTO dto);
}
