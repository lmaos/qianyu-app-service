package com.clmcat.qianyu.social.like.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.social.like.model.dto.LikeCommentTargetDto;
import com.clmcat.qianyu.social.like.model.dto.LikeMomentTargetDto;
import com.clmcat.qianyu.social.like.model.vo.LikeStatusVo;
import com.clmcat.qianyu.social.like.service.LikeViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 点赞接口。
 * <p>
 * 当前类使用 {@link ApiController} 暴露作品点赞、评论点赞及点赞状态查询能力。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "点赞接口", description = "提供作品点赞、评论点赞以及点赞状态查询能力。")
@ApiController
@RequestMapping("/api/social/like")
@LoginVerify
public class LikeController {
    @Resource
    private LikeViewServiceBiz likeViewServiceBiz;

    /**
     * 点赞作品。
     *
     * @param userId 当前登录用户ID
     * @param dto 作品参数，必须提供 momentId
     * @return 当前点赞状态
     */
    @Operation(summary = "点赞作品", description = "参数说明：userId 为当前登录用户ID；dto.momentId 为要点赞的作品ID。")
    @PostMapping("/moment")
    public boolean likeMoment(@Parameter(hidden = true) @Token long userId, @RequestBody(description = "作品点赞参数") @Params LikeMomentTargetDto dto) {
        return likeViewServiceBiz.likeMoment(userId, dto);
    }

    /**
     * 取消点赞作品。
     *
     * @param userId 当前登录用户ID
     * @param dto 作品参数，必须提供 momentId
     * @return 当前取消后的状态
     */
    @Operation(summary = "取消点赞作品", description = "参数说明：userId 为当前登录用户ID；dto.momentId 为要取消点赞的作品ID。")
    @PostMapping("/moment/cancel")
    public boolean cancelLikeMoment(@Parameter(hidden = true) @Token long userId, @RequestBody(description = "取消作品点赞参数") @Params LikeMomentTargetDto dto) {
        return likeViewServiceBiz.cancelLikeMoment(userId, dto);
    }

    /**
     * 查询当前用户是否已点赞作品。
     *
     * @param userId 当前登录用户ID
     * @param dto 作品参数，必须提供 momentId
     * @return 点赞状态 VO
     */
    @Operation(summary = "查询作品点赞状态", description = "参数说明：userId 为当前登录用户ID；dto.momentId 为作品ID。")
    @GetMapping("/moment/status")
    public LikeStatusVo momentStatus(@Parameter(hidden = true) @Token long userId, @ParameterObject @Params LikeMomentTargetDto dto) {
        return likeViewServiceBiz.getMomentLikeStatus(userId, dto);
    }

    /**
     * 点赞评论或回复。
     *
     * @param userId 当前登录用户ID
     * @param dto 评论参数，必须提供 commentId
     * @return 当前点赞状态
     */
    @Operation(summary = "点赞评论", description = "参数说明：userId 为当前登录用户ID；dto.commentId 为要点赞的评论或回复ID。")
    @PostMapping("/comment")
    public boolean likeComment(@Parameter(hidden = true) @Token long userId, @RequestBody(description = "评论点赞参数") @Params LikeCommentTargetDto dto) {
        return likeViewServiceBiz.likeComment(userId, dto);
    }

    /**
     * 取消点赞评论或回复。
     *
     * @param userId 当前登录用户ID
     * @param dto 评论参数，必须提供 commentId
     * @return 当前取消后的状态
     */
    @Operation(summary = "取消点赞评论", description = "参数说明：userId 为当前登录用户ID；dto.commentId 为要取消点赞的评论或回复ID。")
    @PostMapping("/comment/cancel")
    public boolean cancelLikeComment(@Parameter(hidden = true) @Token long userId, @RequestBody(description = "取消评论点赞参数") @Params LikeCommentTargetDto dto) {
        return likeViewServiceBiz.cancelLikeComment(userId, dto);
    }

    /**
     * 查询当前用户是否已点赞评论或回复。
     *
     * @param userId 当前登录用户ID
     * @param dto 评论参数，必须提供 commentId
     * @return 点赞状态 VO
     */
    @Operation(summary = "查询评论点赞状态", description = "参数说明：userId 为当前登录用户ID；dto.commentId 为评论或回复ID。")
    @GetMapping("/comment/status")
    public LikeStatusVo commentStatus(@Parameter(hidden = true) @Token long userId, @ParameterObject @Params LikeCommentTargetDto dto) {
        return likeViewServiceBiz.getCommentLikeStatus(userId, dto);
    }
}
