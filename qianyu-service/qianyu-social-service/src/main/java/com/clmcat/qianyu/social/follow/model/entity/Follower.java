package com.clmcat.qianyu.social.follow.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 粉丝表（记录用户的粉丝）
 */
@Data
@Table("follower")
public class Follower {

    @Id(keyType = KeyType.None)   // 手动分配雪花ID
    private Long id;

    @Column("followee_id")
    private Long followeeId;

    @Column("follower_id")
    private Long followerId;

    @Column("client_time")
    private Long clientTime;

    @Column("server_time")
    private LocalDateTime serverTime;
}