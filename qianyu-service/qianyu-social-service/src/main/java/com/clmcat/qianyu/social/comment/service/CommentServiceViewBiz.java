package com.clmcat.qianyu.social.comment.service;

import com.clmcat.qianyu.social.api.comment.model.dto.CommentDto;
import com.clmcat.qianyu.social.api.like.model.dto.CommentLikeDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentIdDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentIdsDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentMomentQueryDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentPublishDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentReplyQueryDto;
import com.clmcat.qianyu.social.comment.model.entity.status.Status;
import com.clmcat.qianyu.social.comment.model.vo.CommentPageVo;
import com.clmcat.qianyu.social.comment.model.vo.CommentVo;
import com.clmcat.qianyu.social.comment.support.CommentSupport;
import com.clmcat.qianyu.social.like.service.LikeServiceBiz;
import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.PpcUserInfoListDto;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentServiceViewBiz {
    @Resource
    private CommentServiceBiz commentServiceBiz;

    @DubboReference
    private UserApi userApi;

    @Resource
    private LikeServiceBiz likeServiceBiz;

    /**
     * 发布评论或回复。
     *
     * @param authorId 当前登录用户ID
     * @param dto 发布参数，顶级评论或二级回复均通过该 DTO 传递
     * @return 评论 VO
     */
    public CommentVo publish(long authorId, CommentPublishDto dto) {
        Status.AUTHOR_REQUIRED.assertThrowResEx(CommentSupport.isNullOrNonPositive(authorId));
        CommentDto commentDto = CommentSupport.toCommentDto(authorId, dto);
        boolean saved = commentServiceBiz.save(commentDto);
        Status.COMMENT_SAVE_FAIL.assertThrowResEx(!saved);

        CommentVo vo = CommentSupport.toCommentVo(commentDto);
        return enrichAuthorInfo(vo);
    }

    /**
     * 查询单条评论。
     *
     * @param dto 查询参数，必须提供 commentId
     * @return 评论 VO
     */
    public CommentVo getComment(long viewerId, CommentIdDto dto) {
        long commentId = dto == null ? 0L : Objects.requireNonNullElse(dto.getCommentId(), 0L);
        Status.COMMENT_ID_REQUIRED.assertThrowResEx(CommentSupport.isNullOrNonPositive(commentId));
        CommentDto commentDto = commentServiceBiz.getCommentById(commentId);
        Status.COMMENT_NOT_FOUND.assertThrowResEx(commentDto == null);

        CommentVo vo = CommentSupport.toCommentVo(commentDto);
        vo = enrichAuthorInfo(vo);
        return enrichHasLike(viewerId, vo);
    }

    /**
     * 批量查询评论。
     *
     * @param dto 查询参数，支持多个 commentId
     * @return 评论 VO 列表
     */
    public List<CommentVo> getCommentList(long viewerId, CommentIdsDto dto) {
        List<Long> commentIds = CommentSupport.normalizeCommentIds(dto);
        if (commentIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<CommentVo> vos = enrichHasLike(viewerId, enrichAuthorInfo(CommentSupport.toCommentVoList(commentServiceBiz.getCommentByIds(commentIds).getComments())));
        return vos;
    }

    /**
     * 查询作品下的一级评论分页。
     *
     * @param dto 查询参数，包含 momentId、nextCommentId、limit
     * @return 评论分页 VO
     */
    public CommentPageVo getMomentCommentPage(long viewerId, CommentMomentQueryDto dto) {
        long momentId = dto == null ? 0L : Objects.requireNonNullElse(dto.getMomentId(), 0L);
        Status.MOMENT_ID_REQUIRED.assertThrowResEx(CommentSupport.isNullOrNonPositive(momentId));

        int limit = CommentSupport.normalizeLimit(dto == null ? null : dto.getLimit());
        long nextCommentId = CommentSupport.normalizeCursorId(dto == null ? null : dto.getNextCommentId());
        List<CommentDto> commentDtos = commentServiceBiz.getCommentListByMomentId(momentId, nextCommentId, limit + 1).getComments();

        boolean hasMore = commentDtos.size() > limit;
        if (hasMore) {
            commentDtos = new ArrayList<>(commentDtos.subList(0, limit));
        }
        long nextCursorId = hasMore && !commentDtos.isEmpty() ? commentDtos.get(commentDtos.size() - 1).getCommentId() : 0L;
        List<CommentVo> commentVos = enrichHasLike(viewerId, enrichAuthorInfo(CommentSupport.toCommentVoList(commentDtos)));
        return CommentPageVo.builder()
                .momentId(momentId)
                .parentCommentId(0L)
                .nextCommentId(nextCursorId)
                .hasMore(hasMore)
                .commentList(commentVos)
                .build();
    }

    /**
     * 查询一级评论下的回复分页。
     *
     * @param viewerId 当前查看者ID，用于填充 hasLike
     * @param dto 查询参数，包含 parentCommentId、nextCommentId、limit
     * @return 回复分页 VO
     */
    public CommentPageVo getReplyCommentPage(long viewerId, CommentReplyQueryDto dto) {
        long parentCommentId = dto == null ? 0L : Objects.requireNonNullElse(dto.getParentCommentId(), 0L);
        Status.COMMENT_ID_REQUIRED.assertThrowResEx(CommentSupport.isNullOrNonPositive(parentCommentId));

        int limit = CommentSupport.normalizeLimit(dto == null ? null : dto.getLimit());
        long nextCommentId = CommentSupport.normalizeCursorId(dto == null ? null : dto.getNextCommentId());
        List<CommentDto> commentDtos = commentServiceBiz.getReplyListByParentCommentId(parentCommentId, nextCommentId, limit + 1).getComments();

        boolean hasMore = commentDtos.size() > limit;
        if (hasMore) {
            commentDtos = new ArrayList<>(commentDtos.subList(0, limit));
        }
        long nextCursorId = hasMore && !commentDtos.isEmpty() ? commentDtos.get(commentDtos.size() - 1).getCommentId() : 0L;
        Long momentId = commentDtos.isEmpty() ? null : commentDtos.get(0).getMomentId();
        List<CommentVo> commentVos = enrichHasLike(viewerId, enrichAuthorInfo(CommentSupport.toCommentVoList(commentDtos)));
        return CommentPageVo.builder()
                .momentId(momentId)
                .parentCommentId(parentCommentId)
                .nextCommentId(nextCursorId)
                .hasMore(hasMore)
                .commentList(commentVos)
                .build();
    }

    /**
     * 删除自己的评论。
     *
     * @param authorId 当前登录用户ID
     * @param dto 删除参数，必须提供 commentId
     * @return 删除结果
     */
    public boolean deleteComment(long authorId, CommentIdDto dto) {
        Status.AUTHOR_REQUIRED.assertThrowResEx(CommentSupport.isNullOrNonPositive(authorId));
        long commentId = dto == null ? 0L : Objects.requireNonNullElse(dto.getCommentId(), 0L);
        Status.COMMENT_ID_REQUIRED.assertThrowResEx(CommentSupport.isNullOrNonPositive(commentId));

        CommentDto commentDto = commentServiceBiz.getCommentById(commentId);
        Status.COMMENT_NOT_FOUND.assertThrowResEx(commentDto == null);
        Status.COMMENT_DELETE_DENIED.assertThrowResEx(!Objects.equals(commentDto.getAuthorId(), authorId));

        boolean deleted = commentServiceBiz.deleteCommentByIdAndAuthorId(commentId, authorId);
        Status.COMMENT_DELETE_FAIL.assertThrowResEx(!deleted);
        return true;
    }

    // ========== 辅助方法 ==========

    /**
     * 填充单条 CommentVo 的 hasLike。
     */
    private CommentVo enrichHasLike(long userId, CommentVo vo) {
        if (vo == null || userId <= 0 || vo.getCommentId() == null) {
            return vo;
        }
        List<CommentVo> result = enrichHasLike(userId, Collections.singletonList(vo));
        return result.isEmpty() ? vo : result.get(0);
    }

    /**
     * 批量填充 CommentVo 的 hasLike（当前用户是否已点赞），返回新列表。
     * <p>
     * 通过 LikeServiceBiz 逐条查询点赞状态。
     *
     * @param userId     当前用户ID
     * @param commentVos 待填充的 CommentVo 列表
     * @return 填充后的新列表
     */
    private List<CommentVo> enrichHasLike(long userId, List<CommentVo> commentVos) {
        if (userId <= 0 || commentVos == null || commentVos.isEmpty()) {
            return new ArrayList<>();
        }
        List<CommentVo> result = new ArrayList<>(commentVos.size());
        for (CommentVo vo : commentVos) {
            if (vo == null || vo.getCommentId() == null) {
                result.add(vo);
                continue;
            }
            try {
                CommentLikeDto likeDto = CommentLikeDto.builder()
                        .userId(userId)
                        .commentId(vo.getCommentId())
                        .build();
                boolean hasLike = likeServiceBiz.existsCommentLike(likeDto);
                result.add(vo.toBuilder()
                        .hasLike(hasLike)
                        .build());
            } catch (Exception e) {
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 填充单条 CommentVo 的作者昵称和头像。
     */
    private CommentVo enrichAuthorInfo(CommentVo vo) {
        if (vo == null || vo.getAuthorId() == null) {
            return vo;
        }
        List<CommentVo> result = enrichAuthorInfo(Collections.singletonList(vo));
        return result.isEmpty() ? vo : result.get(0);
    }

    /**
     * 批量填充 CommentVo 的作者昵称和头像，返回新列表。
     * <p>
     * 通过 UserApi 批量查询作者信息（userId → nickname/avatar），
     * 避免 N+1 查询。
     *
     * @param commentVos 待填充的 CommentVo 列表
     * @return 填充后的新列表
     */
    private List<CommentVo> enrichAuthorInfo(List<CommentVo> commentVos) {
        if (commentVos == null || commentVos.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 收集所有不重复的 authorId
        List<Long> authorIds = commentVos.stream()
                .filter(Objects::nonNull)
                .map(CommentVo::getAuthorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (authorIds.isEmpty()) {
            return new ArrayList<>(commentVos);
        }

        // 2. 批量查询用户信息
        java.util.Map<Long, RpcUserInfoDto> userMap;
        try {
            PpcUserInfoListDto userList = userApi.getUserInfoList(authorIds);
            if (userList == null || userList.getUsers() == null) {
                return new ArrayList<>(commentVos);
            }
            userMap = new java.util.HashMap<>(userList.getUsers().size());
            for (RpcUserInfoDto user : userList.getUsers()) {
                if (user != null && user.getUserId() != null) {
                    userMap.put(user.getUserId(), user);
                }
            }
        } catch (Exception e) {
            return new ArrayList<>(commentVos);
        }

        // 3. 构建新列表（不修改入参）
        List<CommentVo> result = new ArrayList<>(commentVos.size());
        for (CommentVo vo : commentVos) {
            if (vo == null || vo.getAuthorId() == null) {
                result.add(vo);
                continue;
            }
            RpcUserInfoDto userInfo = userMap.get(vo.getAuthorId());
            if (userInfo != null) {
                result.add(vo.toBuilder()
                        .nickname(userInfo.getNickname())
                        .avatar(userInfo.getAvatar())
                        .build());
            } else {
                result.add(vo);
            }
        }
        return result;
    }
}
