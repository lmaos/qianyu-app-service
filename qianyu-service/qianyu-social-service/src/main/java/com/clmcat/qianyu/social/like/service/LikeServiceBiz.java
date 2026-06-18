package com.clmcat.qianyu.social.like.service;

import com.clmcat.qianyu.social.api.base.model.dto.UserSocialCounterDto;
import com.clmcat.qianyu.social.api.comment.model.dto.CommentDto;
import com.clmcat.qianyu.social.api.like.LikeApi;
import com.clmcat.qianyu.social.api.like.model.dto.CommentLikeDto;
import com.clmcat.qianyu.social.api.like.model.dto.MomentLikeDto;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentIdListDto;
import com.clmcat.qianyu.social.base.service.UserSocialCounterServiceBiz;
import com.clmcat.qianyu.social.comment.service.CommentServiceBiz;
import com.clmcat.qianyu.social.comment.support.CommentSupport;
import com.clmcat.qianyu.social.like.mapper.CommentLikeMapper;
import com.clmcat.qianyu.social.like.mapper.MomentLikeMapper;
import com.clmcat.qianyu.social.like.model.entity.CommentLike;
import com.clmcat.qianyu.social.like.model.entity.MomentLike;
import com.clmcat.qianyu.social.like.model.entity.status.Status;
import com.clmcat.qianyu.social.like.support.LikeSupport;
import com.clmcat.qianyu.social.moment.service.MomentServiceBiz;
import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@DubboService
public class LikeServiceBiz implements LikeApi {
    @Resource
    private MomentLikeMapper momentLikeMapper;
    @Resource
    private CommentLikeMapper commentLikeMapper;
    @Resource
    private MomentServiceBiz momentServiceBiz;
    @Resource
    private CommentServiceBiz commentServiceBiz;
    @Resource
    private UserSocialCounterServiceBiz userSocialCounterServiceBiz;

    /**
     * 点赞作品。
     *
     * @param dto 作品点赞参数
     * @return true 表示当前已点赞
     */
    @Override
    @Transactional
    public boolean likeMoment(MomentLikeDto dto) {
        verifyMomentLike(dto);
        MomentDto momentDto = momentServiceBiz.getMomentById(dto.getMomentId());
        Status.MOMENT_NOT_FOUND.assertThrowResEx(momentDto == null);
        MomentLike momentLike = LikeSupport.newMomentLike(dto, momentDto);

        try {
            if (momentLikeMapper.insertSelective(momentLike) <= 0) {
                return false;
            }
        } catch (DuplicateKeyException e) {
            return true;
        }

        momentServiceBiz.incrementMomentLikes(dto.getMomentId(), 1L);
        userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                .userId(dto.getUserId())
                .likedPostCount(1L)
                .build());
        if (!dto.getUserId().equals(dto.getAuthorId())) {
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getAuthorId())
                    .likeCount(1L)
                    .build());
        }
        return true;
    }

    /**
     * 取消点赞作品。
     *
     * @param dto 作品点赞参数
     * @return true 表示当前已取消
     */
    @Override
    @Transactional
    public boolean cancelLikeMoment(MomentLikeDto dto) {
        verifyMomentLike(dto);
        MomentLike momentLike = findMomentLike(dto.getMomentId(), dto.getUserId());
        if (momentLike == null) {
            return true;
        }
        if (momentLikeMapper.deleteById(momentLike.getId()) <= 0) {
            return false;
        }

        momentServiceBiz.incrementMomentLikes(dto.getMomentId(), -1L);
        userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                .userId(dto.getUserId())
                .likedPostCount(-1L)
                .build());
        if (!dto.getUserId().equals(momentLike.getAuthorId())) {
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(momentLike.getAuthorId())
                    .likeCount(-1L)
                    .build());
        }
        return true;
    }

    /**
     * 查询是否已点赞作品。
     *
     * @param dto 作品点赞参数
     * @return true 表示已点赞
     */
    @Override
    public boolean existsMomentLike(MomentLikeDto dto) {
        if (dto == null) {
            return false;
        }
        return findMomentLike(dto.getMomentId(), dto.getUserId()) != null;
    }

    /**
     * 点赞评论或回复。
     *
     * @param dto 评论点赞参数
     * @return true 表示当前已点赞
     */
    @Override
    @Transactional
    public boolean likeComment(CommentLikeDto dto) {
        verifyCommentLike(dto);
        CommentDto commentDto = commentServiceBiz.getCommentById(dto.getCommentId());
        Status.COMMENT_NOT_FOUND.assertThrowResEx(commentDto == null);
        Status.COMMENT_NOT_LIKEABLE.assertThrowResEx(commentDto.getStatus() == null || !commentDto.getStatus().equals(CommentSupport.COMMENT_STATUS_SHOW));
        CommentLike commentLike = LikeSupport.newCommentLike(dto, commentDto);

        try {
            if (commentLikeMapper.insertSelective(commentLike) <= 0) {
                return false;
            }
        } catch (DuplicateKeyException e) {
            return true;
        }

        commentServiceBiz.incrementCommentLikes(dto.getCommentId(), 1L);
        if (!dto.getUserId().equals(dto.getAuthorId())) {
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getAuthorId())
                    .likeCount(1L)
                    .build());
        }
        return true;
    }

    /**
     * 取消点赞评论或回复。
     *
     * @param dto 评论点赞参数
     * @return true 表示当前已取消
     */
    @Override
    @Transactional
    public boolean cancelLikeComment(CommentLikeDto dto) {
        verifyCommentLike(dto);
        CommentLike commentLike = findCommentLike(dto.getCommentId(), dto.getUserId());
        if (commentLike == null) {
            return true;
        }
        if (commentLikeMapper.deleteById(commentLike.getId()) <= 0) {
            return false;
        }

        commentServiceBiz.incrementCommentLikes(dto.getCommentId(), -1L);
        if (!dto.getUserId().equals(commentLike.getAuthorId())) {
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(commentLike.getAuthorId())
                    .likeCount(-1L)
                    .build());
        }
        return true;
    }

    /**
     * 查询是否已点赞评论或回复。
     *
     * @param dto 评论点赞参数
     * @return true 表示已点赞
     */
    @Override
    public boolean existsCommentLike(CommentLikeDto dto) {
        if (dto == null) {
            return false;
        }
        return findCommentLike(dto.getCommentId(), dto.getUserId()) != null;
    }

    @Override
    public MomentIdListDto getLikedMomentIdsByUserId(long userId, long nextId, int limit) {
        if (userId <= 0 || limit <= 0) {
            return MomentIdListDto.EMPTY;
        }
        if (nextId <= 0) {
            nextId = Long.MAX_VALUE;
        }
        List<Long> momentIds = momentLikeMapper.selectLikedMomentIdsByUserId(userId, nextId, limit);
        if (momentIds == null || momentIds.isEmpty()) {
            return MomentIdListDto.EMPTY;
        }
        return MomentIdListDto.builder().momentIds(momentIds).build();
    }

    private MomentLike findMomentLike(Long momentId, Long userId) {
        if (LikeSupport.isNullOrNonPositive(momentId) || LikeSupport.isNullOrNonPositive(userId)) {
            return null;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(MomentLike::getMomentId, momentId)
                .eq(MomentLike::getUserId, userId);
        return momentLikeMapper.selectOneByQuery(queryWrapper);
    }

    private CommentLike findCommentLike(Long commentId, Long userId) {
        if (LikeSupport.isNullOrNonPositive(commentId) || LikeSupport.isNullOrNonPositive(userId)) {
            return null;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId);
        return commentLikeMapper.selectOneByQuery(queryWrapper);
    }

    private void verifyMomentLike(MomentLikeDto dto) {
        Status.USER_REQUIRED.assertThrowResEx(dto == null || LikeSupport.isNullOrNonPositive(dto.getUserId()));
        Status.MOMENT_ID_REQUIRED.assertThrowResEx(dto == null || LikeSupport.isNullOrNonPositive(dto.getMomentId()));
    }

    private void verifyCommentLike(CommentLikeDto dto) {
        Status.USER_REQUIRED.assertThrowResEx(dto == null || LikeSupport.isNullOrNonPositive(dto.getUserId()));
        Status.COMMENT_ID_REQUIRED.assertThrowResEx(dto == null || LikeSupport.isNullOrNonPositive(dto.getCommentId()));
    }
}
