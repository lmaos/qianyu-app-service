package com.clmcat.qianyu.social.api.like;

import com.clmcat.qianyu.social.api.like.model.dto.CommentLikeDto;
import com.clmcat.qianyu.social.api.like.model.dto.MomentLikeDto;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentIdListDto;

/**
 * 点赞 RPC API。
 */
public interface LikeApi {
    /**
     * 点赞作品。
     *
     * @param dto 作品点赞参数
     * @return 点赞后状态；true 表示当前已点赞
     */
    boolean likeMoment(MomentLikeDto dto);

    /**
     * 取消点赞作品。
     *
     * @param dto 作品点赞参数
     * @return 取消后状态；true 表示当前已取消
     */
    boolean cancelLikeMoment(MomentLikeDto dto);

    /**
     * 查询是否已点赞作品。
     *
     * @param dto 作品点赞参数
     * @return true 表示已点赞
     */
    boolean existsMomentLike(MomentLikeDto dto);

    /**
     * 点赞评论或回复。
     *
     * @param dto 评论点赞参数
     * @return 点赞后状态；true 表示当前已点赞
     */
    boolean likeComment(CommentLikeDto dto);

    /**
     * 取消点赞评论或回复。
     *
     * @param dto 评论点赞参数
     * @return 取消后状态；true 表示当前已取消
     */
    boolean cancelLikeComment(CommentLikeDto dto);

    /**
     * 查询是否已点赞评论或回复。
     *
     * @param dto 评论点赞参数
     * @return true 表示已点赞
     */
    boolean existsCommentLike(CommentLikeDto dto);

    /**
     * 查询用户点赞过的作品ID列表，按点赞时间倒序游标分页。
     *
     * @param userId 用户ID
     * @param nextId 游标ID（点赞记录ID），查询 id 小于该值的数据
     * @param limit 查询条数
     * @return 作品ID列表
     */
    MomentIdListDto getLikedMomentIdsByUserId(long userId, long nextId, int limit);
}
