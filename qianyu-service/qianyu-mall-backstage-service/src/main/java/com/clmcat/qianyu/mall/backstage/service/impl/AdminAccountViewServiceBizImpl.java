package com.clmcat.qianyu.mall.backstage.service.impl;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.backstage.mapper.*;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminLoginDTO;
import com.clmcat.qianyu.mall.backstage.model.entity.*;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminAccountInfoVO;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminLoginVO;
import com.clmcat.qianyu.mall.backstage.service.AdminAccountViewServiceBiz;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 运营账号服务实现。
 * <p>login：BCrypt 校验密码 → 失败 fail_count++（达5锁15min）+ 写 login_log(result=0)；成功 fail_count=0 +
 * 颁 Redis db1 session（admin:session:{token} → JSON{adminId,permCodes}，TTL 2h）+ 写 login_log(result=1)。
 */
@Slf4j
@Service
public class AdminAccountViewServiceBizImpl implements AdminAccountViewServiceBiz {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    /** BCrypt cost=12（决策 3 安全策略） */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);
    /** backstage workerId=50（与 fav 42 等错开，避免雪花冲突） */
    private static final CustomSnowflake ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    private static final int FAIL_LOCK_THRESHOLD = 5;

    @Resource private AdminAccountMapper accountMapper;
    @Resource private AdminAccountRoleMapper accountRoleMapper;
    @Resource private AdminRolePermissionMapper rolePermissionMapper;
    @Resource private AdminPermissionMapper permissionMapper;
    @Resource private AdminLoginLogMapper loginLogMapper;
    @Resource(name = "stringRedisTemplate") private StringRedisTemplate redisTemplate;

    @Override
    public AdminLoginVO login(AdminLoginDTO dto, String loginIp, String userAgent) {
        AdminAccount account = accountMapper.selectOneByQuery(
                QueryWrapper.create().where("username = ?", dto.getUsername()));
        ResponseStatus.AUTH_TOKEN_INVALID.assertThrowResEx("账号或密码错误", account == null);
        ResponseStatus.AUTH_TOKEN_INVALID.assertThrowResEx("账号已禁用/冻结",
                account.getStatus() == null || account.getStatus() != 1);
        ResponseStatus.AUTH_TOKEN_INVALID.assertThrowResEx("账号锁定（连续失败达上限，请稍后重试）",
                account.getFailCount() != null && account.getFailCount() >= FAIL_LOCK_THRESHOLD);

        boolean matched = PASSWORD_ENCODER.matches(dto.getPassword(), account.getPwdHash());
        long now = System.currentTimeMillis();
        if (!matched) {
            account.setFailCount((account.getFailCount() == null ? 0 : account.getFailCount()) + 1);
            account.setUpdateTime(now);
            accountMapper.update(account);
            writeLoginLog(account, loginIp, userAgent, 0, "密码错误");
            ResponseStatus.AUTH_TOKEN_INVALID.assertThrowResEx("账号或密码错误", true);
        }

        // 成功：清失败计数 + 记录登录 + 颁 session
        account.setFailCount(0);
        account.setLastLoginAt(now);
        account.setLastLoginIp(loginIp);
        account.setUpdateTime(now);
        accountMapper.update(account);

        List<String> permCodes = getPermCodes(account.getId());
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(
                BackstageLoginVerifyFunction.SESSION_KEY_PREFIX + token,
                buildSessionJson(account.getId(), account.getUsername(), permCodes),
                BackstageLoginVerifyFunction.SESSION_TTL_HOURS, TimeUnit.HOURS);
        // BG-03：写反向索引 admin:session:idx:{adminId} → SET(token)，供 disable 按 adminId 即时吊销全部 session。
        // TTL 比 session 长 1h，确保滑续期内 idx 不先于 session 过期（verifyLogin 会同步续期 idx）。
        String idxKey = BackstageLoginVerifyFunction.SESSION_KEY_PREFIX + "idx:" + account.getId();
        redisTemplate.opsForSet().add(idxKey, token);
        redisTemplate.expire(idxKey, BackstageLoginVerifyFunction.SESSION_TTL_HOURS + 1, TimeUnit.HOURS);
        writeLoginLog(account, loginIp, userAgent, 1, null);
        log.info("运营账号登录成功 adminId={} username={}", account.getId(), account.getUsername());

        return AdminLoginVO.builder()
                .adminToken(token).adminId(account.getId())
                .username(account.getUsername()).realName(account.getRealName())
                .permCodes(permCodes).build();
    }

    @Override
    public void logout(String adminToken) {
        if (adminToken == null || adminToken.isEmpty()) {
            return;
        }
        String key = BackstageLoginVerifyFunction.SESSION_KEY_PREFIX + adminToken;
        // BG-03：登出时从反向索引移除该 token，避免 idx SET 无界增长。
        Long adminId = readAdminIdFromSession(key);
        redisTemplate.delete(key);
        if (adminId != null) {
            redisTemplate.opsForSet().remove(
                    BackstageLoginVerifyFunction.SESSION_KEY_PREFIX + "idx:" + adminId, adminToken);
        }
    }

    @Override
    public AdminAccountInfoVO getAccountInfo(Long adminId) {
        AdminAccount account = accountMapper.selectOneById(adminId);
        ResponseStatus.AUTH_TOKEN_INVALID.assertThrowResEx("账号不存在", account == null);
        return AdminAccountInfoVO.builder()
                .adminId(adminId).username(account.getUsername())
                .realName(account.getRealName()).permCodes(getPermCodes(adminId)).build();
    }

    /** 聚合 permCodes：account → roles → permissions → perm_code（去重）。 */
    private List<String> getPermCodes(Long adminId) {
        List<AdminAccountRole> accountRoles = accountRoleMapper.selectListByQuery(
                QueryWrapper.create().where("account_id = ?", adminId));
        if (accountRoles == null || accountRoles.isEmpty()) return Collections.emptyList();
        List<Long> roleIds = accountRoles.stream().map(AdminAccountRole::getRoleId).collect(Collectors.toList());

        String roleIn = roleIds.stream().map(r -> "?").collect(Collectors.joining(",", "(", ")"));
        List<AdminRolePermission> rolePerms = rolePermissionMapper.selectListByQuery(
                QueryWrapper.create().where("role_id in " + roleIn, roleIds.toArray()));
        if (rolePerms == null || rolePerms.isEmpty()) return Collections.emptyList();
        List<Long> permIds = rolePerms.stream().map(AdminRolePermission::getPermissionId)
                .distinct().collect(Collectors.toList());

        List<AdminPermission> perms = permissionMapper.selectListByIds(permIds);
        if (perms == null) return Collections.emptyList();
        return perms.stream().map(AdminPermission::getPermCode)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private String buildSessionJson(Long adminId, String username, List<String> permCodes) {
        try {
            Map<String, Object> m = new HashMap<>();
            m.put("adminId", adminId);
            m.put("username", username);
            m.put("permCodes", permCodes);
            return MAPPER.writeValueAsString(m);
        } catch (Exception e) {
            throw new RuntimeException("build admin session json fail", e);
        }
    }

    /** BG-03：从 session JSON 解析 adminId（logout 清反向索引用；缺失/异常返回 null）。 */
    @SuppressWarnings("unchecked")
    private Long readAdminIdFromSession(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            Object id = MAPPER.readValue(json, Map.class).get("adminId");
            return id instanceof Number ? ((Number) id).longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void writeLoginLog(AdminAccount account, String ip, String ua, int result, String failReason) {
        long now = System.currentTimeMillis();
        AdminLoginLog loginLog = new AdminLoginLog();
        loginLog.setId(ID_SNOWFLAKE.nextId());
        loginLog.setAccountId(account.getId());
        loginLog.setUsername(account.getUsername());
        loginLog.setLoginAt(now);
        loginLog.setLoginIp(ip);
        loginLog.setUserAgent(ua);
        loginLog.setResult(result);
        loginLog.setFailReason(failReason);
        loginLog.setCreateTime(now);
        loginLogMapper.insert(loginLog);
    }
}
