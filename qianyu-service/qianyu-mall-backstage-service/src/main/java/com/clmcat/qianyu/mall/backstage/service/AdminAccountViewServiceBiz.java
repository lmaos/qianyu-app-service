package com.clmcat.qianyu.mall.backstage.service;

import com.clmcat.qianyu.mall.backstage.model.dto.AdminLoginDTO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminAccountInfoVO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminLoginVO;

/** 运营账号服务（登录/登出/账号信息）。 */
public interface AdminAccountViewServiceBiz {
    /** 登录：BCrypt 校验 + 失败计数 + 颁 Redis db1 session + 写 login_log。 */
    AdminLoginVO login(AdminLoginDTO dto, String loginIp, String userAgent);

    /** 登出：删 Redis session。 */
    void logout(String adminToken);

    /** 当前账号信息 + permCodes（前端动态路由硬前置）。 */
    AdminAccountInfoVO getAccountInfo(Long adminId);
}
