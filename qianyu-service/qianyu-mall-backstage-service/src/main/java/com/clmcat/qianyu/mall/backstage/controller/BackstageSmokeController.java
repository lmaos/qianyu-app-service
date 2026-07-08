package com.clmcat.qianyu.mall.backstage.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.mall.backstage.support.BackstageLoginVerifyFunction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PoC smoke Controller：验证 {@code @LoginVerify(loginVerify=BackstageLoginVerifyFunction.class, token="X-Admin-Token")}
 * 框架扩展点端到端可用。
 *
 * <p>验证方法：重启后端 + 手动塞 Redis
 * {@code SET "admin:session:test-token" '{"adminId":1,"permCodes":[]}'}，
 * 调 {@code GET /api/admin/ping}（header {@code X-Admin-Token: test-token}）→ 200 + adminId=1；无/错 token → 401。
 *
 * <p>P1a-2 login Controller 落地后本 smoke 可保留作健康检查或移除。
 */
@Tag(name = "Backstage PoC", description = "运营后台 PoC smoke（P1a-1）")
@ApiController
@RequestMapping("/api/admin")
@LoginVerify(loginVerify = BackstageLoginVerifyFunction.class, token = "X-Admin-Token")
public class BackstageSmokeController {

    @Operation(summary = "PoC ping（验证 BackstageLoginVerifyFunction 扩展点）")
    @GetMapping("/ping")
    public Map<String, Object> ping(@Token Long adminId) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ok", true);
        r.put("adminId", adminId);
        r.put("source", "BackstageLoginVerifyFunction");
        return r;
    }
}
