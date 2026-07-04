package com.clmcat.qianyu.social.visitor.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客记录表（谁看过我）。
 */
@Data
@Table("user_visitor")
public class UserVisitor {

    @Id(keyType = KeyType.None)  // 手动分配雪花ID
    private Long id;

    @Column("visitor_id")
    private Long visitorId;

    @Column("visitee_id")
    private Long visiteeId;

    @Column("visit_count")
    private Integer visitCount;

    @Column("is_new")
    private Integer isNew;

    @Column("client_time")
    private Long clientTime;

    @Column("server_time")
    private LocalDateTime serverTime;

    @Column("create_time_server")
    private LocalDateTime createTimeServer;
}
