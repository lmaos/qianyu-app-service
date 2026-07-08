package com.clmcat.qianyu.mall.backstage.service.impl;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.backstage.mapper.AdminRoleMapper;
import com.clmcat.qianyu.mall.backstage.mapper.AdminRolePermissionMapper;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRoleAssignPermissionsDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRoleCreateDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRolePageQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminRoleUpdateDTO;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminRole;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminRolePermission;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminRoleVO;
import com.clmcat.qianyu.mall.backstage.service.AdminRoleViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运营角色管理服务实现（CRUD + 权限分配）。
 *
 * <p>实现要点：
 * <ul>
 *   <li>雪花 workerId=53（与登录 42 / 账号 52 / 权限 54 / 日志 55 错开）；</li>
 *   <li>QueryWrapper 占位符参数化（禁拼接）；</li>
 *   <li>assignPermissions 先 deleteByQuery(role_id) 清旧关联，再批量 insertSelective（幂等全量覆盖）；</li>
 *   <li>page 富化 permissionIds：避免 N+1。</li>
 * </ul>
 */
@Slf4j
@Service
public class AdminRoleViewServiceBizImpl implements AdminRoleViewServiceBiz {

    private static final CustomSnowflake ROLE_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    private static final CustomSnowflake ROLE_PERM_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    @Resource private AdminRoleMapper roleMapper;
    @Resource private AdminRolePermissionMapper rolePermissionMapper;

    @Override
    public Page<AdminRoleVO> page(AdminRolePageQueryDTO dto) {
        int pageNum = dto == null || dto.getPageNum() == null || dto.getPageNum() <= 0 ? 1 : dto.getPageNum();
        int pageSize = dto == null || dto.getPageSize() == null || dto.getPageSize() <= 0 ? 10 : dto.getPageSize();

        QueryWrapper qw = QueryWrapper.create().where("deleted = ?", 0);
        if (dto != null && dto.getKeyword() != null && !dto.getKeyword().isEmpty()) {
            String like = "%" + dto.getKeyword() + "%";
            qw.and("(role_code like ? or role_name like ?)", like, like);
        }
        qw.orderBy("create_time DESC");

        Page<AdminRole> rawPage = roleMapper.paginate(Page.of(pageNum, pageSize), qw);
        List<AdminRole> records = rawPage == null || rawPage.getRecords() == null
                ? Collections.emptyList() : rawPage.getRecords();
        if (records.isEmpty()) {
            Page<AdminRoleVO> empty = new Page<>(pageNum, pageSize);
            empty.setRecords(Collections.emptyList());
            empty.setTotalRow(rawPage == null ? 0 : rawPage.getTotalRow());
            return empty;
        }

        // 富化 permissionIds：一次性查全部 role_permission，避免 N+1
        List<Long> roleIds = records.stream().map(AdminRole::getId).collect(Collectors.toList());
        Map<Long, List<Long>> permIdsByRole = loadPermissionIdsBatch(roleIds);

        List<AdminRoleVO> voList = records.stream().map(r -> AdminRoleVO.builder()
                .id(r.getId())
                .roleCode(r.getRoleCode())
                .roleName(r.getRoleName())
                .status(r.getStatus())
                .remark(r.getRemark())
                .createTime(r.getCreateTime())
                .permissionIds(permIdsByRole.getOrDefault(r.getId(), Collections.emptyList()))
                .build()).collect(Collectors.toList());

        Page<AdminRoleVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(rawPage.getTotalRow());
        return result;
    }

    @Override
    public Long create(AdminRoleCreateDTO dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("角色创建参数缺失", dto == null);
        ResponseStatus.P_NOTNULL.assertThrowResEx("角色码不能为空",
                dto.getRoleCode() == null || dto.getRoleCode().isEmpty());
        ResponseStatus.P_NOTNULL.assertThrowResEx("角色名不能为空",
                dto.getRoleName() == null || dto.getRoleName().isEmpty());

        AdminRole exists = roleMapper.selectOneByQuery(
                QueryWrapper.create().where("role_code = ?", dto.getRoleCode()).and("deleted = ?", 0));
        ResponseStatus.U_EXIST_ACCOUNT.assertThrowResEx("角色码已存在", exists != null);

        long now = System.currentTimeMillis();
        AdminRole role = new AdminRole();
        role.setId(ROLE_ID_SNOWFLAKE.nextId());
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setRemark(dto.getRemark());
        role.setStatus(1);
        role.setCreateTime(now);
        role.setUpdateTime(now);
        role.setDeleted(0);
        roleMapper.insert(role);
        log.info("运营角色创建成功 id={} roleCode={}", role.getId(), role.getRoleCode());
        return role.getId();
    }

    @Override
    public void update(AdminRoleUpdateDTO dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("角色更新参数缺失", dto == null || dto.getId() == null);
        AdminRole exists = roleMapper.selectOneById(dto.getId());
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx("角色不存在", exists == null);
        exists.setRoleName(dto.getRoleName());
        exists.setRemark(dto.getRemark());
        exists.setUpdateTime(System.currentTimeMillis());
        roleMapper.update(exists);
        log.info("运营角色更新 id={}", dto.getId());
    }

    @Override
    public List<Long> getPermissions(Long roleId) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("角色ID不能为空", roleId == null);
        List<AdminRolePermission> rels = rolePermissionMapper.selectListByQuery(
                QueryWrapper.create().where("role_id = ?", roleId));
        if (rels == null || rels.isEmpty()) return Collections.emptyList();
        return rels.stream().map(AdminRolePermission::getPermissionId)
                .distinct().collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(AdminRoleAssignPermissionsDTO dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("分配权限参数缺失", dto == null || dto.getRoleId() == null);
        // 先删旧关联
        rolePermissionMapper.deleteByQuery(
                QueryWrapper.create().where("role_id = ?", dto.getRoleId()));
        // 再批量插新关联
        List<Long> permIds = dto.getPermissionIds();
        if (permIds == null || permIds.isEmpty()) {
            log.info("运营角色权限已清空 roleId={}", dto.getRoleId());
            return;
        }
        long now = System.currentTimeMillis();
        for (Long permId : permIds) {
            AdminRolePermission rel = new AdminRolePermission();
            rel.setRoleId(dto.getRoleId());
            rel.setPermissionId(permId);
            rel.setCreateTime(now);
            rolePermissionMapper.insertSelective(rel);
        }
        log.info("运营角色权限分配完成 roleId={} permCount={}", dto.getRoleId(), permIds.size());
    }

    /**
     * 批量加载多个角色的权限 ID 列表（避免 page 内 N+1）。
     */
    private Map<Long, List<Long>> loadPermissionIdsBatch(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return Collections.emptyMap();
        String inPlaceholder = roleIds.stream().map(id -> "?")
                .collect(Collectors.joining(",", "(", ")"));
        List<AdminRolePermission> rels = rolePermissionMapper.selectListByQuery(
                QueryWrapper.create().where("role_id in " + inPlaceholder, roleIds.toArray()));
        if (rels == null || rels.isEmpty()) return Collections.emptyMap();
        return rels.stream().collect(Collectors.groupingBy(AdminRolePermission::getRoleId,
                Collectors.mapping(AdminRolePermission::getPermissionId,
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().distinct().collect(Collectors.toList())))));
    }
}
