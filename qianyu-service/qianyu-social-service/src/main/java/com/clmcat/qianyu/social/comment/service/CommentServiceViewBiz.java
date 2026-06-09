package com.clmcat.qianyu.social.comment.service;

import com.clmcat.qianyu.social.api.comment.model.dto.CommentDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentIdDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentIdsDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentMomentQueryDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentPublishDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentReplyQueryDto;
import com.clmcat.qianyu.social.comment.model.entity.status.Status;
import com.clmcat.qianyu.social.comment.model.vo.CommentPageVo;
import com.clmcat.qianyu.social.comment.model.vo.CommentVo;
import com.clmcat.qianyu.social.comment.support.CommentSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CommentServiceViewBiz {
    @Resource
    private CommentServiceBiz commentServiceBiz;

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
        return CommentSupport.toCommentVo(commentDto);
    }

    /**
     * 查询单条评论。
     *
     * @param dto 查询参数，必须提供 commentId
     * @return 评论 VO
     */
    public CommentVo getComment(CommentIdDto dto) {
        long commentId = dto == null ? 0L : Objects.requireNonNullElse(dto.getCommentId(), 0L);
        Status.COMMENT_ID_REQUIRED.assertThrowResEx(CommentSupport.isNullOrNonPositive(commentId));
        CommentDto commentDto = commentServiceBiz.getCommentById(commentId);
        Status.COMMENT_NOT_FOUND.assertThrowResEx(commentDto == null);
        return CommentSupport.toCommentVo(commentDto);
    }

    /**
     * 批量查询评论。
     *
     * @param dto 查询参数，支持多个 commentId
     * @return 评论 VO 列表
     */
    public List<CommentVo> getCommentList(CommentIdsDto dto) {
        List<Long> commentIds = CommentSupport.normalizeCommentIds(dto);
        if (commentIds.isEmpty()) {
            return new ArrayList<>();
        }
        return CommentSupport.toCommentVoList(commentServiceBiz.getCommentByIds(commentIds).getComments());
    }

    /**
     * 查询作品下的一级评论分页。
     *
     * @param dto 查询参数，包含 momentId、nextCommentId、limit
     * @return 评论分页 VO
     */
    public CommentPageVo getMomentCommentPage(CommentMomentQueryDto dto) {
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
        return CommentPageVo.builder()
                .momentId(momentId)
                .parentCommentId(0L)
                .nextCommentId(nextCursorId)
                .hasMore(hasMore)
                .commentList(CommentSupport.toCommentVoList(commentDtos))
                .build();
    }

    /**
     * 查询一级评论下的回复分页。
     *
     * @param dto 查询参数，包含 parentCommentId、nextCommentId、limit
     * @return 回复分页 VO
     */
    public CommentPageVo getReplyCommentPage(CommentReplyQueryDto dto) {
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
        return CommentPageVo.builder()
                .momentId(momentId)
                .parentCommentId(parentCommentId)
                .nextCommentId(nextCursorId)
                .hasMore(hasMore)
                .commentList(CommentSupport.toCommentVoList(commentDtos))
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
}
