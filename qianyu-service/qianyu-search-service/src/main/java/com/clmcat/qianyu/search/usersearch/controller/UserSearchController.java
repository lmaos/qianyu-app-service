package com.clmcat.qianyu.search.usersearch.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.qianyu.search.api.model.dto.UserSearchResultDto;
import com.clmcat.qianyu.search.usersearch.service.UserSearchServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户搜索 HTTP 接口。
 * <p>
 * 搜索接口暂不要求登录，后续如需登录校验，加 {@code @LoginVerify} 注解即可。
 *
 * @author ark-home
 * @date 2026-07-07
 */
@Tag(name = "用户搜索接口", description = "提供昵称模糊搜索和昵称索引更新。")
@ApiController
@RequestMapping("/api/user/search")
public class UserSearchController {

    @Resource
    private UserSearchServiceBiz userSearchService;

    /**
     * 按昵称模糊搜索用户。
     * <p>
     * 三级优先级：完整匹配 → 前缀匹配 → NGram 模糊匹配，结果去重，最多返回 100 条。
     *
     * @param keyword 搜索关键词
     * @return 匹配的用户列表
     */
    @Operation(summary = "按昵称搜索用户",
            description = "按昵称模糊搜索，优先级：完整匹配 > 前缀匹配 > NGram模糊匹配，结果去重最多 100 条。")
    @GetMapping("/nickname")
    public List<UserSearchResultDto> searchNickname(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword) {
        return userSearchService.searchByNickname(keyword);
    }

    /**
     * 更新昵称索引（供测试和管理后台调用）。
     * <p>
     * 正常流程由用户修改昵称时通过 Dubbo 调用 {@link UserSearchServiceBiz#updateNickname}。
     *
     * @param userId   用户ID
     * @param nickname 新昵称
     */
    @Operation(summary = "更新昵称索引", description = "手动更新指定用户的昵称索引。正常流程由用户修改昵称时通过 Dubbo 自动同步。")
    @PostMapping("/update_nickname")
    public void updateNickname(
            @Parameter(description = "用户ID", required = true) @RequestParam long userId,
            @Parameter(description = "新昵称", required = true) @RequestParam String nickname) {
        userSearchService.updateNickname(userId, nickname);
    }
}
