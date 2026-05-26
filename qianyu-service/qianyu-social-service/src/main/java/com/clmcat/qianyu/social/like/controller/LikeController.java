package com.clmcat.qianyu.social.like.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.social.like.model.dto.LikeCommentTargetDto;
import com.clmcat.qianyu.social.like.model.dto.LikeMomentTargetDto;
import com.clmcat.qianyu.social.like.model.vo.LikeStatusVo;
import com.clmcat.qianyu.social.like.service.LikeViewServiceBiz;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;

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
    @RequestMapping("/moment")
    public boolean likeMoment(@Token long userId, @Params LikeMomentTargetDto dto) {
        return likeViewServiceBiz.likeMoment(userId, dto);
    }

    /**
     * 取消点赞作品。
     *
     * @param userId 当前登录用户ID
     * @param dto 作品参数，必须提供 momentId
     * @return 当前取消后的状态
     */
    @RequestMapping("/moment/cancel")
    public boolean cancelLikeMoment(@Token long userId, @Params LikeMomentTargetDto dto) {
        return likeViewServiceBiz.cancelLikeMoment(userId, dto);
    }

    /**
     * 查询当前用户是否已点赞作品。
     *
     * @param userId 当前登录用户ID
     * @param dto 作品参数，必须提供 momentId
     * @return 点赞状态 VO
     */
    @RequestMapping("/moment/status")
    public LikeStatusVo momentStatus(@Token long userId, @Params LikeMomentTargetDto dto) {
        return likeViewServiceBiz.getMomentLikeStatus(userId, dto);
    }

    /**
     * 点赞评论或回复。
     *
     * @param userId 当前登录用户ID
     * @param dto 评论参数，必须提供 commentId
     * @return 当前点赞状态
     */
    @RequestMapping("/comment")
    public boolean likeComment(@Token long userId, @Params LikeCommentTargetDto dto) {
        return likeViewServiceBiz.likeComment(userId, dto);
    }

    /**
     * 取消点赞评论或回复。
     *
     * @param userId 当前登录用户ID
     * @param dto 评论参数，必须提供 commentId
     * @return 当前取消后的状态
     */
    @RequestMapping("/comment/cancel")
    public boolean cancelLikeComment(@Token long userId, @Params LikeCommentTargetDto dto) {
        return likeViewServiceBiz.cancelLikeComment(userId, dto);
    }

    /**
     * 查询当前用户是否已点赞评论或回复。
     *
     * @param userId 当前登录用户ID
     * @param dto 评论参数，必须提供 commentId
     * @return 点赞状态 VO
     */
    @RequestMapping("/comment/status")
    public LikeStatusVo commentStatus(@Token long userId, @Params LikeCommentTargetDto dto) {
        return likeViewServiceBiz.getCommentLikeStatus(userId, dto);
    }
}
