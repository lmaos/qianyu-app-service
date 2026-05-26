package com.clmcat.qianyu.social.comment.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.social.api.comment.model.dto.CommentContent;
import com.clmcat.qianyu.social.api.comment.model.dto.CommentDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentIdsDto;
import com.clmcat.qianyu.social.comment.model.dto.CommentPublishDto;
import com.clmcat.qianyu.social.comment.model.entity.Comment;
import com.clmcat.qianyu.social.comment.model.vo.CommentVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class CommentSupport {
    public static final int COMMENT_LEVEL_TOP = 1;
    public static final int COMMENT_LEVEL_REPLY = 2;
    public static final int COMMENT_STATUS_SHOW = 0;
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public static final CustomSnowflake COMMENT_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    public static CommentDto toCommentDto(long authorId, CommentPublishDto dto) {
        if (dto == null) {
            return null;
        }
        CommentDto commentDto = new CommentDto();
        commentDto.setMomentId(dto.getMomentId());
        commentDto.setAuthorId(authorId);
        commentDto.setParentCommentId(dto.getParentCommentId());
        commentDto.setReplyCommentId(dto.getReplyCommentId());
        commentDto.setContent(dto.getContent());
        return commentDto;
    }

    public static Comment newComment(CommentDto dto) {
        if (dto == null) {
            return null;
        }
        long commentId = COMMENT_ID_SNOWFLAKE.nextId();
        long clientTime = SnowflakeSupport.parseTimeBySnowflake(COMMENT_ID_SNOWFLAKE, commentId);
        dto.setCommentId(commentId);
        dto.setClientTime(clientTime);
        dto.setLikes(0L);
        dto.setReplies(0L);
        dto.setStatus(COMMENT_STATUS_SHOW);
        return toComment(dto);
    }

    public static Comment toComment(CommentDto dto) {
        if (dto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setCommentId(dto.getCommentId());
        comment.setMomentId(dto.getMomentId());
        comment.setMomentAuthorId(dto.getMomentAuthorId());
        comment.setAuthorId(dto.getAuthorId());
        comment.setParentCommentId(dto.getParentCommentId());
        comment.setReplyCommentId(dto.getReplyCommentId());
        comment.setReplyUserId(dto.getReplyUserId());
        comment.setCommentLevel(dto.getCommentLevel());
        comment.setContent(dto.getContent());
        comment.setStatus(dto.getStatus());
        comment.setLikes(dto.getLikes());
        comment.setReplies(dto.getReplies());
        comment.setClientTime(dto.getClientTime());
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDto dto = new CommentDto();
        dto.setCommentId(comment.getCommentId());
        dto.setMomentId(comment.getMomentId());
        dto.setMomentAuthorId(comment.getMomentAuthorId());
        dto.setAuthorId(comment.getAuthorId());
        dto.setParentCommentId(comment.getParentCommentId());
        dto.setReplyCommentId(comment.getReplyCommentId());
        dto.setReplyUserId(comment.getReplyUserId());
        dto.setCommentLevel(comment.getCommentLevel());
        dto.setContent(comment.getContent());
        dto.setStatus(comment.getStatus());
        dto.setLikes(comment.getLikes());
        dto.setReplies(comment.getReplies());
        dto.setClientTime(comment.getClientTime());
        return dto;
    }

    public static List<CommentDto> toCommentDtoList(Collection<Comment> comments) {
        List<CommentDto> list = new ArrayList<>();
        if (comments == null) {
            return list;
        }
        for (Comment comment : comments) {
            CommentDto dto = toCommentDto(comment);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

    public static CommentVo toCommentVo(CommentDto dto) {
        if (dto == null) {
            return null;
        }
        return CommentVo.builder()
                .commentId(dto.getCommentId())
                .momentId(dto.getMomentId())
                .momentAuthorId(dto.getMomentAuthorId())
                .authorId(dto.getAuthorId())
                .parentCommentId(dto.getParentCommentId())
                .replyCommentId(dto.getReplyCommentId())
                .replyUserId(dto.getReplyUserId())
                .commentLevel(dto.getCommentLevel())
                .content(dto.getContent())
                .status(dto.getStatus())
                .likes(dto.getLikes())
                .replies(dto.getReplies())
                .clientTime(dto.getClientTime())
                .build();
    }

    public static List<CommentVo> toCommentVoList(Collection<CommentDto> comments) {
        List<CommentVo> list = new ArrayList<>();
        if (comments == null) {
            return list;
        }
        for (CommentDto dto : comments) {
            CommentVo vo = toCommentVo(dto);
            if (vo != null) {
                list.add(vo);
            }
        }
        return list;
    }

    public static List<Long> normalizeCommentIds(CommentIdsDto dto) {
        LinkedHashSet<Long> commentIds = new LinkedHashSet<>();
        if (dto == null) {
            return new ArrayList<>();
        }
        if (dto.getCommentIds() != null) {
            for (Long commentId : dto.getCommentIds()) {
                if (!isNullOrNonPositive(commentId)) {
                    commentIds.add(commentId);
                }
            }
        }
        if (StringUtils.isNotBlank(dto.getCommentIdsText())) {
            String[] values = StringUtils.split(dto.getCommentIdsText(), ",");
            if (values != null) {
                for (String value : values) {
                    long commentId = NumberUtils.toLong(StringUtils.trim(value), 0L);
                    if (commentId > 0) {
                        commentIds.add(commentId);
                    }
                }
            }
        }
        return new ArrayList<>(commentIds);
    }

    public static int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    public static long normalizeCursorId(Long nextCommentId) {
        if (isNullOrNonPositive(nextCommentId)) {
            return Long.MAX_VALUE;
        }
        return nextCommentId;
    }

    public static boolean hasText(CommentContent content) {
        return content != null
                && content.getText() != null
                && StringUtils.isNotBlank(content.getText().getText());
    }

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.longValue() <= 0;
    }
}
