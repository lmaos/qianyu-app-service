package com.clmcat.qianyu.social.moment.model.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Feed 卡片分页 VO。
 */
@Getter
@Builder
public class FeedCardPageVo {
    /** 下一页游标（momentId），0 表示无更多 */
    private long nextCursor;

    /** 是否还有更多数据 */
    private boolean hasMore;

    /** 卡片列表 */
    private List<FeedCardVo> datas;
}
