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
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
    @RequestMapping("/publish")
    @LoginVerify
    public CommentVo publish(@Token long userId, @Params CommentPublishDto dto) {
        return commentServiceViewBiz.publish(userId, dto);
    }

    /**
     * 查询单条评论详情。
     *
     * @param dto 查询参数，必须提供 commentId
     * @return 评论 VO
     */
    @RequestMapping("/get")
    public CommentVo get(@Params CommentIdDto dto) {
        return commentServiceViewBiz.getComment(dto);
    }

    /**
     * 批量查询评论。
     *
     * @param dto 查询参数，可通过 JSON commentIds 或逗号分隔字符串传入
     * @return 评论列表
     */
    @RequestMapping("/list")
    public List<CommentVo> list(@Params CommentIdsDto dto) {
        return commentServiceViewBiz.getCommentList(dto);
    }

    /**
     * 查询作品下的一级评论列表。
     *
     * @param dto 查询参数，包含 momentId、nextCommentId、limit
     * @return 评论分页 VO
     */
    @RequestMapping("/moment/list")
    public CommentPageVo momentList(@Params CommentMomentQueryDto dto) {
        return commentServiceViewBiz.getMomentCommentPage(dto);
    }

    /**
     * 查询一级评论下的二级回复列表。
     *
     * @param dto 查询参数，包含 parentCommentId、nextCommentId、limit
     * @return 回复分页 VO
     */
    @RequestMapping("/reply/list")
    public CommentPageVo replyList(@Params CommentReplyQueryDto dto) {
        return commentServiceViewBiz.getReplyCommentPage(dto);
    }

    /**
     * 删除自己的评论。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 删除参数，必须提供 commentId
     * @return 删除结果
     */
    @RequestMapping("/delete")
    @LoginVerify
    public boolean delete(@Token long userId, @Params CommentIdDto dto) {
        return commentServiceViewBiz.deleteComment(userId, dto);
    }
}
