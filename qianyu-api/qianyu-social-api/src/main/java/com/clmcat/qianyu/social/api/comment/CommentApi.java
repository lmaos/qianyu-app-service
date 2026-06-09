package com.clmcat.qianyu.social.api.comment;

import com.clmcat.qianyu.social.api.comment.model.dto.CommentDto;
import com.clmcat.qianyu.social.api.comment.model.dto.CommentListDto;

import java.util.List;

/**
 * 评论 RPC API。
 */
public interface CommentApi {
    /**
     * 发布评论或回复。
     *
     * @param comment 评论 DTO；顶级评论时 parentCommentId=0，二级回复时指定 parentCommentId / replyCommentId
     * @return 发布是否成功
     */
    boolean save(CommentDto comment);

    /**
     * 查询单条评论。
     *
     * @param commentId 评论ID
     * @return 评论 DTO；不存在返回 null
     */
    CommentDto getCommentById(long commentId);

    /**
     * 批量查询评论。
     *
     * @param commentIds 评论ID 集合
     * @return 评论 DTO 列表
     */
    CommentListDto getCommentByIds(List<Long> commentIds);

    /**
     * 查询作品下的一级评论列表。
     *
     * @param momentId 作品ID
     * @param nextCommentId 游标ID，仅查询比它更早的评论
     * @param limit 查询条数
     * @return 评论列表
     */
    CommentListDto getCommentListByMomentId(long momentId, long nextCommentId, int limit);

    /**
     * 查询一级评论下的二级回复列表。
     *
     * @param parentCommentId 一级评论ID
     * @param nextCommentId 游标ID，仅查询比它更早的回复
     * @param limit 查询条数
     * @return 回复列表
     */
    CommentListDto getReplyListByParentCommentId(long parentCommentId, long nextCommentId, int limit);

    /**
     * 删除评论（业务上为逻辑删除）。
     *
     * @param commentId 评论ID
     * @return 删除结果
     */
    boolean deleteCommentById(long commentId);

    /**
     * 删除作者自己的评论（业务上为逻辑删除）。
     *
     * @param commentId 评论ID
     * @param authorId 作者ID
     * @return 删除结果
     */
    boolean deleteCommentByIdAndAuthorId(long commentId, long authorId);
}
