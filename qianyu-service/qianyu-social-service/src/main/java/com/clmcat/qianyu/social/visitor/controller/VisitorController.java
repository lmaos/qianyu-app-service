package com.clmcat.qianyu.social.visitor.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.social.visitor.model.dto.VisitorListQueryDto;
import com.clmcat.qianyu.social.visitor.model.dto.VisitorSelfListQueryDto;
import com.clmcat.qianyu.social.visitor.model.dto.VisitorTargetDto;
import com.clmcat.qianyu.social.visitor.model.dto.VisitorUserQueryDto;
import com.clmcat.qianyu.social.visitor.model.vo.VisitorCountVo;
import com.clmcat.qianyu.social.visitor.model.vo.VisitorPageVo;
import com.clmcat.qianyu.social.visitor.service.VisitorViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * 访客记录接口。
 * <p>
 * 提供访客记录（谁看过我）、浏览历史（我看过谁）、删除和数量统计能力。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 */
@Tag(name = "访客记录接口", description = "提供访客记录、浏览历史、删除和数量统计能力。")
@ApiController
@RequestMapping("/api/social/visitor")
@LoginVerify
public class VisitorController {

    @Resource
    VisitorViewServiceBiz visitorViewServiceBiz;

    /**
     * 记录一次主页访问。
     *
     * @param userId   当前登录用户ID（访问者），来自 Token
     * @param dto      目标用户参数，targetId 为被访问的主页主人
     */
    @Operation(summary = "记录主页访问", description = "参数说明：userId 为当前登录用户（访问者）；dto.targetId 为被访问的主页主人ID。")
    @PostMapping("/record")
    public Map<String, Object> record(@Parameter(hidden = true) @Token long userId, @Params(description = "访问目标参数") VisitorTargetDto dto) {
        visitorViewServiceBiz.recordVisit(userId, dto.getTargetId());
        return Map.of("success", true);
    }

    /**
     * 查询某个用户的访客列表（谁看过 ta）。
     *
     * @param dto 查询参数，userId 为被查询用户，nextId 为游标，limit 为分页大小
     * @return 访客列表分页结果
     */
    @Operation(summary = "查询访客列表", description = "参数说明：dto.userId 为被查询用户ID；dto.nextId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/list")
    public VisitorPageVo visitorList(@ParameterObject @Params VisitorListQueryDto dto) {
        return visitorViewServiceBiz.getVisitorList(dto);
    }

    /**
     * 查询当前登录用户自己的访客列表。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto   查询参数，包含 nextId、limit
     * @return 访客列表分页结果
     */
    @Operation(summary = "查询我的访客列表", description = "参数说明：userId 为当前登录用户ID；dto.nextId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/self/list")
    public VisitorPageVo selfVisitorList(@Parameter(hidden = true) @Token long userId, @ParameterObject @Params VisitorSelfListQueryDto dto) {
        VisitorPageVo page = visitorViewServiceBiz.getSelfVisitorList(userId, dto);
        // 查看后自动清除新访客标记
        visitorViewServiceBiz.clearNewVisitors(userId);
        return page;
    }

    /**
     * 查询某个用户的浏览历史（ta 看过谁）。
     *
     * @param dto 查询参数，userId 为被查询用户，nextId 为游标，limit 为分页大小
     * @return 浏览历史分页结果
     */
    @Operation(summary = "查询浏览历史", description = "参数说明：dto.userId 为被查询用户ID；dto.nextId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/history/list")
    public VisitorPageVo historyList(@ParameterObject @Params VisitorListQueryDto dto) {
        return visitorViewServiceBiz.getHistoryList(dto);
    }

    /**
     * 查询当前登录用户自己的浏览历史。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto   查询参数，包含 nextId、limit
     * @return 浏览历史分页结果
     */
    @Operation(summary = "查询我的浏览历史", description = "参数说明：userId 为当前登录用户ID；dto.nextId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/history/self/list")
    public VisitorPageVo selfHistoryList(@Parameter(hidden = true) @Token long userId, @ParameterObject @Params VisitorSelfListQueryDto dto) {
        return visitorViewServiceBiz.getSelfHistoryList(userId, dto);
    }

    /**
     * 当前登录用户删除自己访客列表中的某个访客记录。
     *
     * @param userId 当前登录用户ID（被访问者/主页主人），来自 Token
     * @param dto   目标用户参数，targetId 为要删除的访问者ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除访客记录", description = "参数说明：userId 为当前登录用户（被访问者）；dto.targetId 为要删除的访问者ID。")
    @PostMapping("/delete")
    public boolean deleteVisitor(@Parameter(hidden = true) @Token long userId, @Params(description = "删除目标参数") VisitorTargetDto dto) {
        return visitorViewServiceBiz.deleteVisitor(userId, dto.getTargetId());
    }

    /**
     * 当前登录用户删除自己浏览历史中的某条记录。
     *
     * @param userId 当前登录用户ID（访问者），来自 Token
     * @param dto   目标用户参数，targetId 为要删除的被访问者ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除浏览历史", description = "参数说明：userId 为当前登录用户（访问者）；dto.targetId 为要删除的被访问者ID。")
    @PostMapping("/history/delete")
    public boolean deleteHistory(@Parameter(hidden = true) @Token long userId, @Params(description = "删除目标参数") VisitorTargetDto dto) {
        return visitorViewServiceBiz.deleteHistory(userId, dto.getTargetId());
    }

    /**
     * 查询某个用户的访客数量。
     *
     * @param userId 被查询用户ID（通过 query param 传入）
     * @return 数量 VO
     */
    @Operation(summary = "查询访客数量", description = "参数说明：dto.userId 为被查询用户ID。")
    @GetMapping("/count")
    public VisitorCountVo count(@ParameterObject @Params VisitorUserQueryDto dto) {
        long userId = dto == null || dto.getUserId() == null ? 0L : dto.getUserId();
        return visitorViewServiceBiz.getVisitorCount(userId);
    }

    /**
     * 查询当前登录用户自己的访客数量。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @return 数量 VO
     */
    @Operation(summary = "查询我的访客数量", description = "参数说明：userId 为当前登录用户ID。")
    @GetMapping("/count/self")
    public VisitorCountVo selfCount(@Parameter(hidden = true) @Token long userId) {
        return visitorViewServiceBiz.getSelfVisitorCount(userId);
    }
}
