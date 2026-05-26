package com.clmcat.qianyu.social.follow.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关注表（记录谁关注了谁）
 */
@Data
@Table("follow")
public class Follow {

    @Id(keyType = KeyType.None)  // 手动分配雪花ID
    private Long id;

    @Column("follower_id")
    private Long followerId;

    @Column("followee_id")
    private Long followeeId;

    @Column("is_friend")
    private Integer isFriend;

    @Column("client_time")
    private Long clientTime;

    @Column("server_time")
    private LocalDateTime serverTime;
}