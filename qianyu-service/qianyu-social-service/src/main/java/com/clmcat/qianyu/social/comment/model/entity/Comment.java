package com.clmcat.qianyu.social.comment.model.entity;

import com.clmcat.qianyu.social.api.comment.model.dto.CommentContent;
import com.clmcat.qianyu.social.comment.typehandler.CommentContentTypeHandler;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("moment_comment")
public class Comment {
    @Id(keyType = KeyType.None)
    @Column(value = "comment_id", comment = "评论ID（雪花）")
    private Long commentId;

    @Column(value = "moment_id", comment = "作品ID")
    private Long momentId;

    @Column(value = "moment_author_id", comment = "作品作者ID")
    private Long momentAuthorId;

    @Column(value = "author_id", comment = "评论作者ID")
    private Long authorId;

    @Column(value = "parent_comment_id", comment = "父评论ID；一级评论=0，二级回复=一级评论ID")
    private Long parentCommentId;

    @Column(value = "reply_comment_id", comment = "回复的评论ID")
    private Long replyCommentId;

    @Column(value = "reply_user_id", comment = "被回复用户ID")
    private Long replyUserId;

    @Column(value = "comment_level", comment = "评论层级：1一级评论，2二级回复")
    private Integer commentLevel;

    @Column(value = "content", comment = "评论内容 JSON(CommentContent)", typeHandler = CommentContentTypeHandler.class)
    private CommentContent content;

    @Column(value = "status", comment = "状态：0显示，1隐藏，2删除")
    private Integer status;

    @Column(value = "likes", comment = "点赞数冗余")
    private Long likes;

    @Column(value = "replies", comment = "回复数冗余")
    private Long replies;

    @Column(value = "client_time", comment = "客户端时间戳（毫秒）")
    private Long clientTime;

    @Column(value = "server_time", comment = "服务端创建时间（微秒）")
    private LocalDateTime serverTime;

    @Column(value = "update_time_server", comment = "服务端更新时间（微秒）")
    private LocalDateTime updateTimeServer;
}
