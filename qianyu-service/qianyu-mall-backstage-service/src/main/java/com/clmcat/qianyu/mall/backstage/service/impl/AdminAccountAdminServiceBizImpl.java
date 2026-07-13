package com.clmcat.qianyu.mall.backstage.service.impl;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.backstage.mapper.AdminAccountMapper;
import com.clmcat.qianyu.mall.backstage.mapper.AdminAccountRoleMapper;
import com.clmcat.qianyu.mall.backstage.mapper.AdminRoleMapper;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountAssignRolesDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountCreateDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountPageQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminAccountUpdateDTO;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminAccount;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminAccountRole;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminRole;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminAccountVO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminRoleVO;
import com.clmcat.qianyu.mall.backstage.service.AdminAccountAdminServiceBiz;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运营账号管理服务实现（CRUD + 角色分配）。
 *
 * <p>实现要点（与 AdminAccountViewServiceBizImpl 对齐）：
 * <ul>
 *   <li>密码 BCrypt(cost=12) 哈希，明文不落库；</li>
 *   <li>雪花 ID workerId=52（与登录服务 42 错开避免冲突）；</li>
 *   <li>QueryWrapper 占位符参数化（P0-0 安全，禁拼接）；</li>
 *   <li>assignRoles 先 deleteByQuery(account_id) 清旧关联，再批量 insertSelective（幂等全量覆盖）；</li>
 *   <li>disable 仅 status=0，session 自然过期，不主动删 Redis（简化）。</li>
 * </ul>
 */
@Slf4j
@Service
public class AdminAccountAdminServiceBizImpl implements AdminAccountAdminServiceBiz {

    /** BCrypt cost=12（决策 3 安全策略，与登录服务一致）。 */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);

    private static final CustomSnowflake ACCOUNT_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    private static final CustomSnowflake ACCOUNT_ROLE_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    @Resource private AdminAccountMapper accountMapper;
    @Resource private AdminAccountRoleMapper accountRoleMapper;
    @Resource private AdminRoleMapper roleMapper;

    @Override
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<AdminAccountVO> page(AdminAccountPageQueryDTO dto) {
        int pageNum = dto == null || dto.getPageNum() == null || dto.getPageNum() <= 0 ? 1 : dto.getPageNum();
        int pageSize = dto == null || dto.getPageSize() == null || dto.getPageSize() <= 0 ? 10 : dto.getPageSize();

        QueryWrapper qw = QueryWrapper.create().where("deleted = ?", 0);
        if (dto != null) {
            if (dto.getStatus() != null) {
                qw.and("status = ?", dto.getStatus());
            }
            if (dto.getKeyword() != null && !dto.getKeyword().isEmpty()) {
                String like = "%" + dto.getKeyword() + "%";
                qw.and("(username like ? or real_name like ? or mobile like ?)",
                        like, like, like);
            }
        }
        qw.orderBy("create_time DESC");

        Page<AdminAccount> rawPage = accountMapper.paginate(Page.of(pageNum, pageSize), qw);
        long totalRow = rawPage == null ? 0 : rawPage.getTotalRow();
        List<AdminAccount> records = rawPage == null || rawPage.getRecords() == null
                ? Collections.emptyList() : rawPage.getRecords();
        if (records.isEmpty()) {
            return com.clmcat.qianyu.mall.api.model.dto.PageResultDTO.<AdminAccountVO>builder()
                    .records(Collections.emptyList()).total(totalRow)
                    .pageNum(pageNum).pageSize(pageSize).build();
        }

        // 富化 roleNames：一次性查全部 account_role + role，避免 N+1
        List<Long> accountIds = records.stream().map(AdminAccount::getId).collect(Collectors.toList());
        Map<Long, List<String>> roleNamesByAccount = loadRoleNamesBatch(accountIds);

        List<AdminAccountVO> voList = records.stream().map(a -> AdminAccountVO.builder()
                .id(a.getId())
                .username(a.getUsername())
                .realName(a.getRealName())
                .mobile(a.getMobile())
                .email(a.getEmail())
                .status(a.getStatus())
                .lastLoginAt(a.getLastLoginAt())
                .createTime(a.getCreateTime())
                .roleNames(roleNamesByAccount.getOrDefault(a.getId(), Collections.emptyList()))
                .build()).collect(Collectors.toList());

        return com.clmcat.qianyu.mall.api.model.dto.PageResultDTO.<AdminAccountVO>builder()
                .records(voList).total(totalRow)
                .pageNum(pageNum).pageSize(pageSize).build();
    }

    @Override
    public Long create(AdminAccountCreateDTO dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("账号创建参数缺失", dto == null);
        ResponseStatus.P_NOTNULL.assertThrowResEx("用户名不能为空",
                dto.getUsername() == null || dto.getUsername().isEmpty());
        ResponseStatus.P_NOTNULL.assertThrowResEx("密码不能为空",
                dto.getPassword() == null || dto.getPassword().isEmpty());

        // username 唯一性校验
        AdminAccount exists = accountMapper.selectOneByQuery(
                QueryWrapper.create().where("username = ?", dto.getUsername()).and("deleted = ?", 0));
        ResponseStatus.U_EXIST_ACCOUNT.assertThrowResEx("用户名已存在", exists != null);

        long now = System.currentTimeMillis();
        AdminAccount account = new AdminAccount();
        account.setId(ACCOUNT_ID_SNOWFLAKE.nextId());
        account.setUsername(dto.getUsername());
        account.setPwdHash(PASSWORD_ENCODER.encode(dto.getPassword()));
        account.setPwdSalt("");  // BCrypt hash 自带盐；pwd_salt NOT NULL 设空串占位
        account.setRealName(dto.getRealName());
        account.setMobile(dto.getMobile());
        account.setEmail(dto.getEmail());
        account.setStatus(1);
        account.setFailCount(0);
        account.setCreateTime(now);
        account.setUpdateTime(now);
        account.setDeleted(0);
        accountMapper.insert(account);
        log.info("运营账号创建成功 id={} username={}", account.getId(), account.getUsername());
        return account.getId();
    }

    @Override
    public void update(AdminAccountUpdateDTO dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("账号更新参数缺失", dto == null || dto.getId() == null);
        AdminAccount exists = accountMapper.selectOneById(dto.getId());
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx("账号不存在", exists == null);

        exists.setRealName(dto.getRealName());
        exists.setMobile(dto.getMobile());
        exists.setEmail(dto.getEmail());
        exists.setUpdateTime(System.currentTimeMillis());
        accountMapper.update(exists);
        log.info("运营账号资料更新 id={}", dto.getId());
    }

    @Override
    public void disable(Long accountId) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("账号ID不能为空", accountId == null);
        AdminAccount exists = accountMapper.selectOneById(accountId);
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx("账号不存在", exists == null);
        exists.setStatus(0);
        exists.setUpdateTime(System.currentTimeMillis());
        accountMapper.update(exists);
        log.info("运营账号禁用 id={}（session 自然过期，不主动删 Redis）", accountId);
    }

    @Override
    public void resetPwd(AdminAccountUpdateDTO dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("重置密码参数缺失",
                dto == null || dto.getId() == null || dto.getPassword() == null || dto.getPassword().isEmpty());
        AdminAccount exists = accountMapper.selectOneById(dto.getId());
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx("账号不存在", exists == null);
        exists.setPwdHash(PASSWORD_ENCODER.encode(dto.getPassword()));
        exists.setFailCount(0);
        exists.setUpdateTime(System.currentTimeMillis());
        accountMapper.update(exists);
        log.info("运营账号密码重置 id={}", dto.getId());
    }

    @Override
    public List<AdminRoleVO> getRoles(Long accountId) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("账号ID不能为空", accountId == null);
        List<AdminAccountRole> accountRoles = accountRoleMapper.selectListByQuery(
                QueryWrapper.create().where("account_id = ?", accountId));
        if (accountRoles == null || accountRoles.isEmpty()) return Collections.emptyList();
        List<Long> roleIds = accountRoles.stream().map(AdminAccountRole::getRoleId).collect(Collectors.toList());

        List<AdminRole> roles = roleMapper.selectListByIds(roleIds);
        if (roles == null) return Collections.emptyList();
        return roles.stream().map(r -> AdminRoleVO.builder()
                .id(r.getId())
                .roleCode(r.getRoleCode())
                .roleName(r.getRoleName())
                .status(r.getStatus())
                .remark(r.getRemark())
                .createTime(r.getCreateTime())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(AdminAccountAssignRolesDTO dto) {
        ResponseStatus.P_NOTNULL.assertThrowResEx("分配角色参数缺失", dto == null || dto.getAccountId() == null);
        // 先删旧关联
        accountRoleMapper.deleteByQuery(
                QueryWrapper.create().where("account_id = ?", dto.getAccountId()));
        // 再批量插新关联
        List<Long> roleIds = dto.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            log.info("运营账号角色已清空 accountId={}", dto.getAccountId());
            return;
        }
        long now = System.currentTimeMillis();
        for (Long roleId : roleIds) {
            AdminAccountRole rel = new AdminAccountRole();
            rel.setAccountId(dto.getAccountId());
            rel.setRoleId(roleId);
            rel.setCreateTime(now);
            accountRoleMapper.insertSelective(rel);
        }
        log.info("运营账号角色分配完成 accountId={} roleCount={}", dto.getAccountId(), roleIds.size());
    }

    /**
     * 批量加载多个账号的角色名（避免 page 内 N+1）。
     * 步骤：account_role in (accountIds) → role_id 分组 → role in (roleIds) → 聚合 role_name。
     */
    private Map<Long, List<String>> loadRoleNamesBatch(List<Long> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) return Collections.emptyMap();
        String inPlaceholder = accountIds.stream().map(id -> "?")
                .collect(Collectors.joining(",", "(", ")"));
        List<AdminAccountRole> rels = accountRoleMapper.selectListByQuery(
                QueryWrapper.create().where("account_id in " + inPlaceholder, accountIds.toArray()));
        if (rels == null || rels.isEmpty()) return Collections.emptyMap();

        // account_id → role_id list
        Map<Long, List<Long>> roleIdsByAccount = rels.stream().collect(
                Collectors.groupingBy(AdminAccountRole::getAccountId,
                        Collectors.mapping(AdminAccountRole::getRoleId, Collectors.toList())));
        List<Long> allRoleIds = roleIdsByAccount.values().stream().flatMap(List::stream)
                .distinct().collect(Collectors.toList());

        List<AdminRole> roles = roleMapper.selectListByIds(allRoleIds);
        Map<Long, String> roleNameById = roles == null ? Collections.emptyMap()
                : roles.stream().collect(Collectors.toMap(AdminRole::getId, AdminRole::getRoleName, (a, b) -> a));

        Map<Long, List<String>> result = new java.util.HashMap<>();
        roleIdsByAccount.forEach((accId, rids) -> {
            List<String> names = new ArrayList<>();
            for (Long rid : rids) {
                String name = roleNameById.get(rid);
                if (name != null) names.add(name);
            }
            result.put(accId, names);
        });
        return result;
    }
}
