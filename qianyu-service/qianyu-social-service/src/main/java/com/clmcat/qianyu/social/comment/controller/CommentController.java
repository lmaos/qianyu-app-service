package com.clmcat.qianyu.social.comment.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.social.comment.model.dto.CommentIdDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentIdsDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentMomentQueryDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentPublishDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentReplyQueryDto;
import com.clmcat.qianyu.social.comment.model.vo.CommentPageVo;
import com.clmcat.qianyu.social.comment.model.vo.CommentVo;
import com.clmcat.qianyu.social.comment.service.CommentServiceViewBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 评论与回复接口。
 * <p>
 * 当前类使用 {@link ApiController} 暴露评论发布、详情、批量查询、作品评论列表、回复列表和删除能力。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "评论回复接口", description = "提供评论发布、评论详情、批量查询、作品评论分页、回复分页和删除能力。")
@ApiController
@RequestMapping("/api/social/comment")
public class CommentController {
    @Resource
    private CommentServiceViewBiz commentServiceViewBiz;

    /**
     * 发布评论或回复。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 发布参数；顶级评论传 momentId + content，回复再补 parentCommentId/replyCommentId
     * @return 评论 VO
     */
    @Operation(summary = "发布评论或回复", description = "参数说明：userId 为当前登录用户ID；dto.momentId 为作品ID；顶级评论 parentCommentId/replyCommentId 传 0，回复时补对应评论ID。")
    @PostMapping("/publish")
    @LoginVerify
    public CommentVo publish(@Parameter(hidden = true) @Token long userId, @RequestBody(description = "评论发布参数") @Params CommentPublishDto dto) {
        return commentServiceViewBiz.publish(userId, dto);
    }

    /**
     * 查询单条评论详情。
     *
     * @param dto 查询参数，必须提供 commentId
     * @return 评论 VO
     */
    @Operation(summary = "查询评论详情", description = "参数说明：dto.commentId 为评论ID。")
    @GetMapping("/get")
    public CommentVo get(@ParameterObject @Params CommentIdDto dto) {
        return commentServiceViewBiz.getComment(dto);
    }

    /**
     * 批量查询评论。
     *
     * @param dto 查询参数，可通过 JSON commentIds 或逗号分隔字符串传入
     * @return 评论列表
     */
    @Operation(summary = "批量查询评论", description = "参数说明：dto.commentIds 适合 JSON 数组；dto.commentIdsText 兼容 query/form 的逗号分隔字符串。")
    @GetMapping("/list")
    public List<CommentVo> list(@ParameterObject @Params CommentIdsDto dto) {
        return commentServiceViewBiz.getCommentList(dto);
    }

    /**
     * 查询作品下的一级评论列表。
     *
     * @param dto 查询参数，包含 momentId、nextCommentId、limit
     * @return 评论分页 VO
     */
    @Operation(summary = "查询作品一级评论列表", description = "参数说明：dto.momentId 为作品ID；dto.nextCommentId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/moment/list")
    public CommentPageVo momentList(@ParameterObject @Params CommentMomentQueryDto dto) {
        return commentServiceViewBiz.getMomentCommentPage(dto);
    }

    /**
     * 查询一级评论下的二级回复列表。
     *
     * @param dto 查询参数，包含 parentCommentId、nextCommentId、limit
     * @return 回复分页 VO
     */
    @Operation(summary = "查询一级评论下的回复列表", description = "参数说明：dto.parentCommentId 为一级评论ID；dto.nextCommentId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/reply/list")
    public CommentPageVo replyList(@ParameterObject @Params CommentReplyQueryDto dto) {
        return commentServiceViewBiz.getReplyCommentPage(dto);
    }

    /**
     * 删除自己的评论。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 删除参数，必须提供 commentId
     * @return 删除结果
     */
    @Operation(summary = "删除评论", description = "参数说明：userId 为当前登录用户ID，仅作者本人可删除；dto.commentId 为待删除评论ID。")
    @PostMapping("/delete")
    @LoginVerify
    public boolean delete(@Parameter(hidden = true) @Token long userId, @RequestBody(description = "评论删除参数") @Params CommentIdDto dto) {
        return commentServiceViewBiz.deleteComment(userId, dto);
    }
}
