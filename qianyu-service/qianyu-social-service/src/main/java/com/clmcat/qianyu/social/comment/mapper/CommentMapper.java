package com.clmcat.qianyu.social.comment.mapper;

import com.clmcat.qianyu.social.comment.model.entity.Comment;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    /**
     * 增减评论点赞数。
     *
     * @param commentId 评论ID
     * @param delta 增量，可为负数
     * @return 影响行数
     */
    @Update("UPDATE moment_comment SET likes = likes + #{delta} WHERE comment_id = #{commentId}")
    int incrementLikes(@Param("commentId") long commentId, @Param("delta") long delta);

    /**
     * 增减一级评论的回复数。
     *
     * @param commentId 一级评论ID
     * @param delta 增量，可为负数
     * @return 影响行数
     */
    @Update("UPDATE moment_comment SET replies = replies + #{delta} WHERE comment_id = #{commentId}")
    int incrementReplies(@Param("commentId") long commentId, @Param("delta") long delta);

    /**
     * 逻辑删除评论，并清空内容。
     *
     * @param commentId 评论ID
     * @return 影响行数
     */
    @Update("UPDATE moment_comment SET status = 2, content = NULL WHERE comment_id = #{commentId}")
    int markDeleted(@Param("commentId") long commentId);
}
