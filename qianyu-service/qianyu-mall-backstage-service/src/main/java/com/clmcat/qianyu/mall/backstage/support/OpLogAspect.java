package com.clmcat.qianyu.mall.backstage.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.backstage.mapper.AdminOpLogMapper;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminOpLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志切面（op_log @Aspect，审计骨架·P1b）。
 *
 * <p>拦截 {@link RequiresPermission} 标注的 Controller 方法（方法级 {@code @annotation} 或类级
 * {@code @within}），在业务方法执行前后采集元数据（adminId/permCode/targetEntity/ip/userAgent/
 * result/costMs/errMsg），同步落 {@link AdminOpLog}。
 *
 * <p>设计要点：
 * <ul>
 *   <li>骨架版：不区分资金类（同步 + before/after 快照）与状态机类（@Async）；统一同步落库，
 *       beforeJson/afterJson 暂不填充（待 P1b 资金类按 findById×2 快照补齐）。</li>
 *   <li>落库失败仅 {@code log.warn}，不抛出——审计不阻断主流程。</li>
 *   <li>主流程异常：result=0（失败）+ errMsg 截断后回写，再 rethrow。</li>
 * </ul>
 *
 * <p>permCode 取 {@code @RequiresPermission.value()[0]}（满足任一即可放行的语义里取首个用于审计标识）。
 * adminId/ip/userAgent 来自 {@link BackstageLoginVerifyFunction} 注入的 request attribute 与 header。
 */
@Aspect
@Component
@Slf4j
public class OpLogAspect {

    /** 审计日志雪花 ID（42 时间位 / 10 机器位 / 11 序列位，项目统一基准）。 */
    private static final CustomSnowflake OP_LOG_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    private final AdminOpLogMapper adminOpLogMapper;

    public OpLogAspect(AdminOpLogMapper adminOpLogMapper) {
        this.adminOpLogMapper = adminOpLogMapper;
    }

    @Around("@annotation(com.clmcat.qianyu.mall.backstage.support.RequiresPermission) || "
            + "@within(com.clmcat.qianyu.mall.backstage.support.RequiresPermission)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        // 采集请求维度元数据（adminId / ip / userAgent）—— 未必有 request（如 RPC 调用），做空安全
        HttpServletRequest request = currentRequest();
        Long adminId = null;
        String ip = null;
        String userAgent = null;
        String username = null;
        if (request != null) {
            Object adminIdAttr = request.getAttribute(BackstageLoginVerifyFunction.ATTR_ADMIN_ID);
            if (adminIdAttr instanceof Number) {
                adminId = ((Number) adminIdAttr).longValue();
            }
            Object usernameAttr = request.getAttribute(BackstageLoginVerifyFunction.ATTR_USERNAME);
            if (usernameAttr instanceof String) {
                username = (String) usernameAttr;
            }
            ip = request.getRemoteAddr();
            userAgent = request.getHeader("User-Agent");
        }

        // 取 permCode（@RequiresPermission 首个值）；同时支持方法级与类级注解
        RequiresPermission anno = method.getAnnotation(RequiresPermission.class);
        if (anno == null) {
            anno = method.getDeclaringClass().getAnnotation(RequiresPermission.class);
        }
        String permCode = (anno != null && anno.value().length > 0) ? anno.value()[0] : null;
        String targetEntity = method.getDeclaringClass().getSimpleName();

        long start = System.currentTimeMillis();
        Object result;
        Integer resultCode = 1; // 1=成功 0=失败
        String errMsg = null;

        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable ex) {
            resultCode = 0;
            // 截断异常消息，避免超长落库
            String msg = ex.getMessage();
            errMsg = msg != null ? msg.substring(0, Math.min(msg.length(), 500)) : ex.getClass().getSimpleName();
            throw ex; // 主流程异常原样抛出
        } finally {
            int costMs = (int) (System.currentTimeMillis() - start);
            try {
                AdminOpLog opLog = new AdminOpLog();
                opLog.setId(OP_LOG_ID_SNOWFLAKE.nextId());
                opLog.setAccountId(adminId);
                opLog.setUsername(username != null ? username : "");
                opLog.setPermCode(permCode);
                opLog.setTargetEntity(targetEntity);
                opLog.setIp(ip);
                opLog.setUserAgent(userAgent);
                opLog.setTs(start);
                opLog.setResult(resultCode);
                opLog.setCostMs(costMs);
                opLog.setErrMsg(errMsg);
                opLog.setCreateTime(System.currentTimeMillis());
                adminOpLogMapper.insert(opLog);
            } catch (Exception ex) {
                // 审计落库失败不影响主流程（主流程异常仍按原 rethrow）
                log.warn("op_log 落库失败 permCode={} entity={} result={}: {}",
                        permCode, targetEntity, resultCode, ex.getMessage());
            }
        }
    }

    /**
     * 取当前 HttpServletRequest（无 web 上下文时返回 null，例如异步/定时任务）。
     */
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }
}
