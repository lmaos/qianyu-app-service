package com.clmcat.qianyu.social.comment.service;

import com.clmcat.qianyu.social.api.base.model.dto.UserSocialCounterDto;
import com.clmcat.qianyu.social.api.comment.CommentApi;
import com.clmcat.qianyu.social.api.comment.model.dto.CommentDto;
import com.clmcat.qianyu.social.api.comment.model.dto.CommentListDto;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;
import com.clmcat.qianyu.social.base.service.UserSocialCounterServiceBiz;
import com.clmcat.qianyu.social.comment.mapper.CommentMapper;
import com.clmcat.qianyu.social.comment.model.entity.Comment;
import com.clmcat.qianyu.social.comment.model.entity.status.Status;
import com.clmcat.qianyu.social.comment.support.CommentSupport;
import com.clmcat.qianyu.social.moment.service.MomentServiceBiz;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@DubboService
public class CommentServiceBiz implements CommentApi {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private MomentServiceBiz momentServiceBiz;
    @Resource
    private UserSocialCounterServiceBiz userSocialCounterServiceBiz;

    /**
     * 发布评论或回复。
     *
     * @param dto 评论 DTO；顶级评论时 parentCommentId=0，回复时指定父评论上下文
     * @return 发布是否成功
     */
    @Override
    @Transactional
    public boolean save(CommentDto dto) {
        verifySave(dto);
        MomentDto momentDto = momentServiceBiz.getMomentById(Objects.requireNonNull(dto.getMomentId()));
        Status.MOMENT_ID_REQUIRED.assertThrowResEx(CommentSupport.isNullOrNonPositive(dto.getMomentId()));
        Status.COMMENT_SAVE_FAIL.assertThrowResEx(momentDto == null);

        fillReplyContext(dto, momentDto);
        Comment comment = CommentSupport.newComment(dto);
        if (commentMapper.insertSelective(comment) <= 0) {
            return false;
        }

        momentServiceBiz.incrementMomentComments(dto.getMomentId(), 1L);
        if (Objects.equals(dto.getCommentLevel(), CommentSupport.COMMENT_LEVEL_REPLY)) {
            incrementCommentReplies(dto.getParentCommentId(), 1L);
        }
        userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                .userId(dto.getAuthorId())
                .commentedPostCount(1L)
                .build());
        if (!Objects.equals(dto.getMomentAuthorId(), dto.getAuthorId())) {
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getMomentAuthorId())
                    .commentCount(1L)
                    .build());
        }
        if (CommentSupport.isNullOrNonPositive(dto.getReplyUserId())
                || Objects.equals(dto.getReplyUserId(), dto.getMomentAuthorId())
                || Objects.equals(dto.getReplyUserId(), dto.getAuthorId())) {
            return true;
        }
        userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                .userId(dto.getReplyUserId())
                .commentCount(1L)
                .build());
        return true;
    }

    /**
     * 查询单条评论。
     *
     * @param commentId 评论ID
     * @return 评论 DTO；不存在返回 null
     */
    @Override
    public CommentDto getCommentById(long commentId) {
        Comment comment = commentMapper.selectOneById(commentId);
        return CommentSupport.toCommentDto(comment);
    }

    /**
     * 批量查询评论。
     *
     * @param commentIds 评论ID 集合
     * @return 评论 DTO 列表
     */
    @Override
    public CommentListDto getCommentByIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return CommentListDto.EMPTY;
        }
        LinkedHashSet<Long> normalizedIds = new LinkedHashSet<>();
        for (Long commentId : commentIds) {
            if (!CommentSupport.isNullOrNonPositive(commentId)) {
                normalizedIds.add(commentId);
            }
        }
        if (normalizedIds.isEmpty()) {
            return CommentListDto.EMPTY;
        }
        List<Comment> comments = commentMapper.selectListByIds(normalizedIds);
        if (comments == null || comments.isEmpty()) {
            return CommentListDto.EMPTY;
        }

        Map<Long, CommentDto> commentMap = new HashMap<>();
        for (Comment comment : comments) {
            commentMap.put(comment.getCommentId(), CommentSupport.toCommentDto(comment));
        }

        List<CommentDto> result = new ArrayList<>(normalizedIds.size());
        for (Long commentId : normalizedIds) {
            CommentDto dto = commentMap.get(commentId);
            if (dto != null) {
                result.add(dto);
            }
        }
        return CommentListDto.builder()
                .comments(result)
                .build();
    }

    /**
     * 查询作品下的一级评论列表。
     *
     * @param momentId 作品ID
     * @param nextCommentId 游标ID
     * @param limit 查询条数
     * @return 评论列表
     */
    @Override
    public CommentListDto getCommentListByMomentId(long momentId, long nextCommentId, int limit) {
        if (momentId <= 0 || limit <= 0) {
            return CommentListDto.EMPTY;
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(Comment::getMomentId, momentId);
        queryWrapper.eq(Comment::getParentCommentId, 0L);
        queryWrapper.lt(Comment::getCommentId, nextCommentId);
        queryWrapper.orderBy(Comment::getCommentId, false);
        queryWrapper.limit(limit);
        List<CommentDto> list = CommentSupport.toCommentDtoList(commentMapper.selectListByQuery(queryWrapper));
        return CommentListDto.builder()
                .comments(list)
                .build();
    }

    /**
     * 查询一级评论下的二级回复列表。
     *
     * @param parentCommentId 一级评论ID
     * @param nextCommentId 游标ID
     * @param limit 查询条数
     * @return 回复列表
     */
    @Override
    public CommentListDto getReplyListByParentCommentId(long parentCommentId, long nextCommentId, int limit) {
        if (parentCommentId <= 0 || limit <= 0) {
            return CommentListDto.EMPTY;
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(Comment::getParentCommentId, parentCommentId);
        queryWrapper.lt(Comment::getCommentId, nextCommentId);
        queryWrapper.orderBy(Comment::getCommentId, false);
        queryWrapper.limit(limit);
        List<CommentDto> list = CommentSupport.toCommentDtoList(commentMapper.selectListByQuery(queryWrapper));
        return CommentListDto.builder()
                .comments(list)
                .build();
    }

    /**
     * 删除评论（逻辑删除）。
     *
     * @param commentId 评论ID
     * @return 删除结果
     */
    @Override
    public boolean deleteCommentById(long commentId) {
        if (commentId <= 0) {
            return false;
        }
        CommentDto dto = getCommentById(commentId);
        if (dto == null) {
            return false;
        }
        if (Objects.equals(dto.getStatus(), 2)) {
            return true;
        }
        return commentMapper.markDeleted(commentId) > 0;
    }

    /**
     * 删除作者自己的评论（逻辑删除）。
     *
     * @param commentId 评论ID
     * @param authorId 作者ID
     * @return 删除结果
     */
    @Override
    public boolean deleteCommentByIdAndAuthorId(long commentId, long authorId) {
        CommentDto dto = getCommentById(commentId);
        if (dto == null || !Objects.equals(dto.getAuthorId(), authorId)) {
            return false;
        }
        if (Objects.equals(dto.getStatus(), 2)) {
            return true;
        }
        return commentMapper.markDeleted(commentId) > 0;
    }

    /**
     * 增减评论点赞数。
     *
     * @param commentId 评论ID
     * @param delta 增量，可为负数
     * @return 更新结果
     */
    public boolean incrementCommentLikes(long commentId, long delta) {
        return commentMapper.incrementLikes(commentId, delta) > 0;
    }

    /**
     * 增减一级评论的回复数。
     *
     * @param commentId 一级评论ID
     * @param delta 增量，可为负数
     * @return 更新结果
     */
    public boolean incrementCommentReplies(long commentId, long delta) {
        return commentMapper.incrementReplies(commentId, delta) > 0;
    }

    private void fillReplyContext(CommentDto dto, MomentDto momentDto) {
        dto.setMomentAuthorId(momentDto.getAuthorId());

        long parentCommentId = CommentSupport.isNullOrNonPositive(dto.getParentCommentId()) ? 0L : dto.getParentCommentId();
        long replyCommentId = CommentSupport.isNullOrNonPositive(dto.getReplyCommentId()) ? 0L : dto.getReplyCommentId();

        CommentDto parentComment = null;
        CommentDto replyComment = null;

        if (parentCommentId > 0) {
            parentComment = getCommentById(parentCommentId);
            Status.PARENT_COMMENT_NOT_FOUND.assertThrowResEx(parentComment == null || !Objects.equals(parentComment.getMomentId(), dto.getMomentId()));
            Status.COMMENT_REPLY_DENIED.assertThrowResEx(!Objects.equals(parentComment.getStatus(), CommentSupport.COMMENT_STATUS_SHOW));
        }
        if (replyCommentId > 0) {
            replyComment = getCommentById(replyCommentId);
            Status.REPLY_COMMENT_NOT_FOUND.assertThrowResEx(replyComment == null || !Objects.equals(replyComment.getMomentId(), dto.getMomentId()));
            Status.COMMENT_REPLY_DENIED.assertThrowResEx(!Objects.equals(replyComment.getStatus(), CommentSupport.COMMENT_STATUS_SHOW));
        }

        if (parentComment == null && replyComment == null) {
            dto.setCommentLevel(CommentSupport.COMMENT_LEVEL_TOP);
            dto.setParentCommentId(0L);
            dto.setReplyCommentId(0L);
            dto.setReplyUserId(0L);
            return;
        }

        if (replyComment != null) {
            long normalizedParentId = Objects.equals(replyComment.getCommentLevel(), CommentSupport.COMMENT_LEVEL_TOP)
                    ? replyComment.getCommentId()
                    : replyComment.getParentCommentId();
            if (parentComment != null) {
                long parentTopId = Objects.equals(parentComment.getCommentLevel(), CommentSupport.COMMENT_LEVEL_TOP)
                        ? parentComment.getCommentId()
                        : parentComment.getParentCommentId();
                Status.COMMENT_REPLY_DENIED.assertThrowResEx(parentTopId != normalizedParentId);
            }
            dto.setParentCommentId(normalizedParentId);
            dto.setReplyCommentId(replyComment.getCommentId());
            dto.setReplyUserId(replyComment.getAuthorId());
            dto.setCommentLevel(CommentSupport.COMMENT_LEVEL_REPLY);
            return;
        }

        long normalizedParentId = Objects.equals(parentComment.getCommentLevel(), CommentSupport.COMMENT_LEVEL_TOP)
                ? parentComment.getCommentId()
                : parentComment.getParentCommentId();
        dto.setParentCommentId(normalizedParentId);
        dto.setReplyCommentId(parentComment.getCommentId());
        dto.setReplyUserId(parentComment.getAuthorId());
        dto.setCommentLevel(CommentSupport.COMMENT_LEVEL_REPLY);
    }

    private void verifySave(CommentDto dto) {
        Status.AUTHOR_REQUIRED.assertThrowResEx(dto == null || CommentSupport.isNullOrNonPositive(dto.getAuthorId()));
        Status.MOMENT_ID_REQUIRED.assertThrowResEx(dto == null || CommentSupport.isNullOrNonPositive(dto.getMomentId()));
        Status.COMMENT_CONTENT_REQUIRED.assertThrowResEx(dto == null || dto.getContent() == null);
        Status.COMMENT_TEXT_REQUIRED.assertThrowResEx(dto == null || !CommentSupport.hasText(dto.getContent()));
    }
}
