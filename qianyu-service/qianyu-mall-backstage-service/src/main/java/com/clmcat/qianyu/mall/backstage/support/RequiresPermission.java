package com.clmcat.qianyu.mall.backstage.support;

import java.lang.annotation.*;

/**
 * 运营接口权限注解（admin 域自建·不动框架）。
 * <p>标在 Controller 方法/类上，{@link RequiresPermissionInterceptor} 校验当前 session 的 permCodes
 * 与注解 value 求交集（满足任一即放行），无交集抛 {@code AUTH_NO_PERMISSION}。
 * <p>permCode 字典 M0 冻结（前后端 1:1），见 06-api-contract.md 五。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    /** 需要的权限码（满足任一即可）。 */
    String[] value();
}
