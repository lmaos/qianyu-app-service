package com.clmcat.qianyu.social.api.visitor.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 访客/浏览记录 DTO（双表共用，isNew 仅 visitor 表有效）。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitorDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 记录ID（雪花） */
    private Long id;

    /** 访问者用户ID */
    private Long visitorId;

    /** 被访问者用户ID */
    private Long visiteeId;

    /** 累计访问次数 */
    private Integer visitCount;

    /** 是否新访客：0已读，1未读（仅 user_visitor 表使用） */
    private Integer isNew;

    /** 最近访问客户端时间戳（毫秒） */
    private Long clientTime;
}
