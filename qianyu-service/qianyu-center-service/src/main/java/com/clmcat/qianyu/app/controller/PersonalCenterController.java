package com.clmcat.qianyu.app.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.app.api.model.dto.ContentPageDto;
import com.clmcat.qianyu.app.api.model.dto.PersonalCenterDto;
import com.clmcat.qianyu.app.service.PersonalCenterServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 个人中心接口。
 * <p>
 * 聚合用户基础信息、统计数据、快捷入口和内容分页列表。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "个人中心接口", description = "提供个人中心聚合数据查询，包括用户信息、统计数据、快捷入口和分页内容列表。")
@ApiController
@RequestMapping("/api/app")
@LoginVerify
public class PersonalCenterController {

    @Resource
    private PersonalCenterServiceBiz personalCenterServiceBiz;

    /**
     * 获取个人中心整体数据。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @return 个人中心数据
     */
    @Operation(
            summary = "获取个人中心数据",
            description = "参数说明：userId 由登录 token 自动解析。返回用户基础信息、统计数据、快捷入口。"
    )
    @GetMapping("/personal/center")
    public PersonalCenterDto personalCenter(@Parameter(hidden = true) @Token long userId) {
        return personalCenterServiceBiz.getPersonalCenter(userId);
    }

    /**
     * 按 tab 分页查询内容列表。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param tab 内容类型：moment（动态）/ work（作品/纯视频）/ like（喜欢）/ history（历史）
     * @param cursor 游标（上一页最后一条的ID），首次传 0
     * @param limit 分页大小，默认 20，最大 50
     * @return 分页结果
     */
    @Operation(
            summary = "分页查询内容列表",
            description = "参数说明：userId 由登录 token 自动解析；tab 支持 moment/work/like/history；cursor 为游标ID，首次传 0；limit 默认 20、最大 50。"
    )
    @GetMapping("/personal/center/contents")
    public ContentPageDto contents(
            @Parameter(hidden = true) @Token long userId,
            @Parameter(description = "内容类型：moment / work / like / history") @RequestParam String tab,
            @Parameter(description = "游标ID，首次传 0") @RequestParam(required = false, defaultValue = "0") long cursor,
            @Parameter(description = "分页大小，默认 20，最大 50") @RequestParam(required = false, defaultValue = "20") int limit) {
        return personalCenterServiceBiz.getContents(userId, tab, cursor, limit);
    }
}
