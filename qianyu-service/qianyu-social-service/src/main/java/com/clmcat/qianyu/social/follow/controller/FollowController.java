package com.clmcat.qianyu.social.follow.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.social.follow.model.dto.FollowListQueryDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowSelfListQueryDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowTargetDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowUserQueryDto;
import com.clmcat.qianyu.social.follow.model.vo.FollowCountVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowPageVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowRelationVo;
import com.clmcat.qianyu.social.follow.service.FollowViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 关注关系接口。
 * <p>
 * 当前类使用 {@link ApiController} 暴露关注、取关、关系查询、关注列表、粉丝列表和数量查询能力。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "关注关系接口", description = "提供关注、取关、关系查询、关注列表、粉丝列表和数量统计能力。")
@ApiController
@RequestMapping("/api/social/follow")
@LoginVerify
public class FollowController {
    @Resource
    FollowViewServiceBiz followViewServiceBiz;

    /**
     * 当前登录用户关注目标用户。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 目标用户参数，targetId 表示要关注的人
     * @return 是否处理成功
     */
    @Operation(summary = "关注用户", description = "参数说明：userId 为当前登录用户ID；dto.targetId 为要关注的目标用户ID。")
    @PostMapping("/follow")
    public boolean follow(@Parameter(hidden = true) @Token long userId, @Params(description = "关注目标参数") FollowTargetDto dto) {
        return followViewServiceBiz.follow(userId, dto);
    }

    /**
     * 当前登录用户取消关注目标用户。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 目标用户参数，targetId 表示要取消关注的人
     * @return 是否处理成功
     */
    @Operation(summary = "取消关注用户", description = "参数说明：userId 为当前登录用户ID；dto.targetId 为要取消关注的目标用户ID。")
    @PostMapping("/cancel")
    public boolean cancel(@Parameter(hidden = true) @Token long userId, @Params(description = "取消关注目标参数") FollowTargetDto dto) {
        return followViewServiceBiz.cancelFollow(userId, dto);
    }

    /**
     * 查看当前登录用户与目标用户之间的关注关系。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 目标用户参数，targetId 表示对方用户
     * @return 关系 VO
     */
    @Operation(summary = "查询关注关系", description = "参数说明：userId 为当前登录用户ID；dto.targetId 为目标用户ID，返回是否关注、是否被关注、是否互关。")
    @GetMapping("/relation")
    public FollowRelationVo relation(@Parameter(hidden = true) @Token long userId, @ParameterObject @Params FollowTargetDto dto) {
        return followViewServiceBiz.getRelation(userId, dto);
    }

    /**
     * 查询用户的关注列表。
     *
     * @param dto 查询参数，userId 为被查询用户，nextId 为游标，limit 为分页大小
     * @return 关注列表分页结果
     */
    @Operation(summary = "查询关注列表", description = "参数说明：dto.userId 为被查询用户ID；dto.nextId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/followee/list")
    public FollowPageVo followList(@ParameterObject @Params FollowListQueryDto dto) {
        return followViewServiceBiz.getFollowList(dto);
    }

    /**
     * 查询当前登录用户自己的关注列表。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 查询参数，包含 nextId、limit
     * @return 关注列表分页结果
     */
    @Operation(summary = "查询我的关注列表", description = "参数说明：userId 为当前登录用户ID；dto.nextId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/followee/self/list")
    public FollowPageVo selfFollowList(@Parameter(hidden = true) @Token long userId, @ParameterObject @Params FollowSelfListQueryDto dto) {
        return followViewServiceBiz.getSelfFollowList(userId, dto);
    }

    /**
     * 查询用户的粉丝列表。
     *
     * @param dto 查询参数，userId 为被查询用户，nextId 为游标，limit 为分页大小
     * @return 粉丝列表分页结果
     */
    @Operation(summary = "查询粉丝列表", description = "参数说明：dto.userId 为被查询用户ID；dto.nextId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/follower/list")
    public FollowPageVo followerList(@ParameterObject @Params FollowListQueryDto dto) {
        return followViewServiceBiz.getFollowerList(dto);
    }

    /**
     * 查询当前登录用户自己的粉丝列表。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 查询参数，包含 nextId、limit
     * @return 粉丝列表分页结果
     */
    @Operation(summary = "查询我的粉丝列表", description = "参数说明：userId 为当前登录用户ID；dto.nextId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/follower/self/list")
    public FollowPageVo selfFollowerList(@Parameter(hidden = true) @Token long userId, @ParameterObject @Params FollowSelfListQueryDto dto) {
        return followViewServiceBiz.getSelfFollowerList(userId, dto);
    }

    /**
     * 查询用户的关注数和粉丝数。
     *
     * @param dto 查询参数，userId 为被查询用户
     * @return 数量 VO
     */
    @Operation(summary = "查询关注和粉丝数量", description = "参数说明：dto.userId 为被查询用户ID。")
    @GetMapping("/count")
    public FollowCountVo count(@ParameterObject @Params FollowUserQueryDto dto) {
        return followViewServiceBiz.getFollowCount(dto);
    }

    /**
     * 查询当前登录用户自己的关注数和粉丝数。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @return 数量 VO
     */
    @Operation(summary = "查询我的关注和粉丝数量", description = "参数说明：userId 为当前登录用户ID，返回自己的关注数和粉丝数。")
    @GetMapping("/count/self")
    public FollowCountVo selfCount(@Parameter(hidden = true) @Token long userId) {
        return followViewServiceBiz.getSelfFollowCount(userId);
    }
}
