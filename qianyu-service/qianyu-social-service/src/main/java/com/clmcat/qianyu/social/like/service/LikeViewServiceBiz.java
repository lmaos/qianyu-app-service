package com.clmcat.qianyu.social.like.service;

import com.clmcat.qianyu.social.api.like.model.dto.CommentLikeDto;
import com.clmcat.qianyu.social.api.like.model.dto.MomentLikeDto;
import com.clmcat.qianyu.social.like.model.dto.LikeCommentTargetDto;
import com.clmcat.qianyu.social.like.model.dto.LikeMomentTargetDto;
import com.clmcat.qianyu.social.like.model.entity.status.Status;
import com.clmcat.qianyu.social.like.model.vo.LikeStatusVo;
import com.clmcat.qianyu.social.like.support.LikeSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class LikeViewServiceBiz {
    @Resource
    private LikeServiceBiz likeServiceBiz;

    /**
     * 点赞作品。
     *
     * @param userId 当前登录用户ID
     * @param dto 作品参数，必须提供 momentId
     * @return 当前点赞状态
     */
    public boolean likeMoment(long userId, LikeMomentTargetDto dto) {
        MomentLikeDto likeDto = LikeSupport.toMomentLikeDto(userId, dto);
        verifyMomentTarget(likeDto);
        return likeServiceBiz.likeMoment(likeDto);
    }

    /**
     * 取消点赞作品。
     *
     * @param userId 当前登录用户ID
     * @param dto 作品参数，必须提供 momentId
     * @return 当前取消后的状态
     */
    public boolean cancelLikeMoment(long userId, LikeMomentTargetDto dto) {
        MomentLikeDto likeDto = LikeSupport.toMomentLikeDto(userId, dto);
        verifyMomentTarget(likeDto);
        return likeServiceBiz.cancelLikeMoment(likeDto);
    }

    /**
     * 查询当前用户是否已点赞作品。
     *
     * @param userId 当前登录用户ID
     * @param dto 作品参数，必须提供 momentId
     * @return 点赞状态 VO
     */
    public LikeStatusVo getMomentLikeStatus(long userId, LikeMomentTargetDto dto) {
        MomentLikeDto likeDto = LikeSupport.toMomentLikeDto(userId, dto);
        verifyMomentTarget(likeDto);
        return LikeSupport.newStatusVo(likeDto.getMomentId(), likeServiceBiz.existsMomentLike(likeDto));
    }

    /**
     * 点赞评论或回复。
     *
     * @param userId 当前登录用户ID
     * @param dto 评论参数，必须提供 commentId
     * @return 当前点赞状态
     */
    public boolean likeComment(long userId, LikeCommentTargetDto dto) {
        CommentLikeDto likeDto = LikeSupport.toCommentLikeDto(userId, dto);
        verifyCommentTarget(likeDto);
        return likeServiceBiz.likeComment(likeDto);
    }

    /**
     * 取消点赞评论或回复。
     *
     * @param userId 当前登录用户ID
     * @param dto 评论参数，必须提供 commentId
     * @return 当前取消后的状态
     */
    public boolean cancelLikeComment(long userId, LikeCommentTargetDto dto) {
        CommentLikeDto likeDto = LikeSupport.toCommentLikeDto(userId, dto);
        verifyCommentTarget(likeDto);
        return likeServiceBiz.cancelLikeComment(likeDto);
    }

    /**
     * 查询当前用户是否已点赞评论或回复。
     *
     * @param userId 当前登录用户ID
     * @param dto 评论参数，必须提供 commentId
     * @return 点赞状态 VO
     */
    public LikeStatusVo getCommentLikeStatus(long userId, LikeCommentTargetDto dto) {
        CommentLikeDto likeDto = LikeSupport.toCommentLikeDto(userId, dto);
        verifyCommentTarget(likeDto);
        return LikeSupport.newStatusVo(likeDto.getCommentId(), likeServiceBiz.existsCommentLike(likeDto));
    }

    private void verifyMomentTarget(MomentLikeDto dto) {
        Status.USER_REQUIRED.assertThrowResEx(dto == null || LikeSupport.isNullOrNonPositive(dto.getUserId()));
        Status.MOMENT_ID_REQUIRED.assertThrowResEx(dto == null || LikeSupport.isNullOrNonPositive(dto.getMomentId()));
    }

    private void verifyCommentTarget(CommentLikeDto dto) {
        Status.USER_REQUIRED.assertThrowResEx(dto == null || LikeSupport.isNullOrNonPositive(dto.getUserId()));
        Status.COMMENT_ID_REQUIRED.assertThrowResEx(dto == null || LikeSupport.isNullOrNonPositive(dto.getCommentId()));
    }
}
