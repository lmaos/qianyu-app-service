package com.clmcat.qianyu.social.like.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("moment_like")
public class MomentLike {
    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "点赞记录ID（雪花）")
    private Long id;

    @Column(value = "moment_id", comment = "作品ID")
    private Long momentId;

    @Column(value = "user_id", comment = "点赞用户ID")
    private Long userId;

    @Column(value = "author_id", comment = "作品作者ID")
    private Long authorId;

    @Column(value = "client_time", comment = "客户端时间戳（毫秒）")
    private Long clientTime;

    @Column(value = "server_time", comment = "服务端创建时间（微秒）")
    private LocalDateTime serverTime;
}
