package com.clmcat.qianyu.social.visitor.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 浏览历史表（我看过谁）。
 */
@Data
@Table("user_history")
public class UserHistory {

    @Id(keyType = KeyType.None)  // 手动分配雪花ID
    private Long id;

    @Column("visitor_id")
    private Long visitorId;

    @Column("visitee_id")
    private Long visiteeId;

    @Column("visit_count")
    private Integer visitCount;

    @Column("client_time")
    private Long clientTime;

    @Column("server_time")
    private LocalDateTime serverTime;

    @Column("create_time_server")
    private LocalDateTime createTimeServer;
}
