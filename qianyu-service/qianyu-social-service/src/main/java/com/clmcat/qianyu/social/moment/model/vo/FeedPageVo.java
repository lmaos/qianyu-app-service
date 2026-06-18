package com.clmcat.qianyu.social.moment.model.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Feed 推荐流分页 VO。
 * <p>
 * 使用 momentId 倒序游标分页，返回 MomentVo 列表。
 */
@Getter
@Builder
public class FeedPageVo {
    /** 下一页游标，传 0 表示没有更多 */
    private long nextCursor;

    /** 是否还有更多数据 */
    private boolean hasMore;

    /** 动态列表 */
    private List<MomentVo> datas;
}
