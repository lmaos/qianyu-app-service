package com.clmcat.qianyu.mall.backstage.service;

import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountAssignRolesDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountCreateDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountPageQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountUpdateDTO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminAccountVO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminRoleVO;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * 运营账号管理服务（CRUD + 角色分配）。
 * <p>区别于 {@link AdminAccountViewServiceBiz}（登录/登出/账号信息），本接口面向
 * 「运营-账号管理」管理后台页面，需 admin:account:manage 权限。
 */
public interface AdminAccountAdminServiceBiz {

    /**
     * 账号分页查询（带 roleNames 富化）。
     * @param dto 分页/关键字/状态过滤；pageNum/pageSize 为 null 走默认 1/10
     * @return 账号分页（records 为 AdminAccountVO）
     */
    Page<AdminAccountVO> page(AdminAccountPageQueryDTO dto);

    /**
     * 创建账号：BCrypt(cost=12) 哈希密码后落库，username 唯一性校验。
     * @return 新账号 ID（雪花）
     */
    Long create(AdminAccountCreateDTO dto);

    /**
     * 更新账号资料（real_name / mobile / email；username / pwd_hash 不动）。
     */
    void update(AdminAccountUpdateDTO dto);

    /**
     * 禁用账号：status=0（session 自然过期，不主动删 Redis——简化）。
     */
    void disable(Long accountId);

    /**
     * 重置密码：BCrypt 哈希新密码落 pwd_hash，fail_count 清 0。
     */
    void resetPwd(AdminAccountUpdateDTO dto);

    /**
     * 查询某账号已分配的角色列表（/api/admin/account/roles）。
     */
    List<AdminRoleVO> getRoles(Long accountId);

    /**
     * 给账号分配角色（先 deleteByQuery account_id 再批量 insertSelective；幂等全量覆盖）。
     */
    void assignRoles(AdminAccountAssignRolesDTO dto);
}
