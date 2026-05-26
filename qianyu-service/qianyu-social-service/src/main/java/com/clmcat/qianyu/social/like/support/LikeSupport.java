package com.clmcat.qianyu.social.like.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.social.api.comment.model.dto.CommentDto;
import com.clmcat.qianyu.social.api.like.model.dto.CommentLikeDto;
import com.clmcat.qianyu.social.api.like.model.dto.MomentLikeDto;
import com.clmcat.qianyu.social.api.moment.model.dto.MomentDto;
import com.clmcat.qianyu.social.like.model.dto.LikeCommentTargetDto;
import com.clmcat.qianyu.social.like.model.dto.LikeMomentTargetDto;
import com.clmcat.qianyu.social.like.model.entity.CommentLike;
import com.clmcat.qianyu.social.like.model.entity.MomentLike;
import com.clmcat.qianyu.social.like.model.vo.LikeStatusVo;

public class LikeSupport {
    public static final CustomSnowflake LIKE_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    public static MomentLikeDto toMomentLikeDto(long userId, LikeMomentTargetDto dto) {
        return MomentLikeDto.builder()
                .userId(userId)
                .momentId(dto == null ? null : dto.getMomentId())
                .build();
    }

    public static CommentLikeDto toCommentLikeDto(long userId, LikeCommentTargetDto dto) {
        return CommentLikeDto.builder()
                .userId(userId)
                .commentId(dto == null ? null : dto.getCommentId())
                .build();
    }

    public static MomentLike newMomentLike(MomentLikeDto dto, MomentDto momentDto) {
        long id = LIKE_ID_SNOWFLAKE.nextId();
        long clientTime = SnowflakeSupport.parseTimeBySnowflake(LIKE_ID_SNOWFLAKE, id);
        dto.setId(id);
        dto.setAuthorId(momentDto.getAuthorId());
        dto.setClientTime(clientTime);

        MomentLike momentLike = new MomentLike();
        momentLike.setId(id);
        momentLike.setMomentId(dto.getMomentId());
        momentLike.setUserId(dto.getUserId());
        momentLike.setAuthorId(dto.getAuthorId());
        momentLike.setClientTime(clientTime);
        return momentLike;
    }

    public static CommentLike newCommentLike(CommentLikeDto dto, CommentDto commentDto) {
        long id = LIKE_ID_SNOWFLAKE.nextId();
        long clientTime = SnowflakeSupport.parseTimeBySnowflake(LIKE_ID_SNOWFLAKE, id);
        dto.setId(id);
        dto.setAuthorId(commentDto.getAuthorId());
        dto.setMomentId(commentDto.getMomentId());
        dto.setClientTime(clientTime);

        CommentLike commentLike = new CommentLike();
        commentLike.setId(id);
        commentLike.setCommentId(dto.getCommentId());
        commentLike.setMomentId(dto.getMomentId());
        commentLike.setUserId(dto.getUserId());
        commentLike.setAuthorId(dto.getAuthorId());
        commentLike.setClientTime(clientTime);
        return commentLike;
    }

    public static LikeStatusVo newStatusVo(Long targetId, boolean liked) {
        return LikeStatusVo.builder()
                .targetId(targetId)
                .liked(liked)
                .build();
    }

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.longValue() <= 0;
    }
}
