package com.clmcat.qianyu.social.visitor.model.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 访客/浏览历史分页 VO。
 */
@Getter
@Builder
public class VisitorPageVo {

    /** 被查询用户ID */
    private Long userId;

    /** 是否有下一页 */
    private boolean hasMore;

    /** 下一页游标：最后一条记录的雪花ID */
    private Long nextId;

    /** 用户列表 */
    private List<VisitorUserVo> userList;
}
