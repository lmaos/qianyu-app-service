package com.clmcat.qianyu.social.api.visitor;

import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorCountDto;
import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorDto;
import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorListDto;

/**
 * 访客统计与记录 RPC API。
 */
public interface VisitorApi {

    /**
     * 记录一次主页访问（双写 user_visitor + user_history）。
     *
     * @param dto 访问参数，包含 visitorId 和 visiteeId
     */
    void recordVisit(VisitorDto dto);

    /**
     * 查询用户的新访客数。
     *
     * @param userId 用户ID
     * @return 访客统计 DTO
     */
    VisitorCountDto getVisitorCount(long userId);

    /**
     * 查询谁看过我（访客列表），按 id DESC 游标分页。
     *
     * @param visiteeId 被访问者ID（主页主人）
     * @param nextId    游标：上一页最后一条的雪花ID
     * @param limit     分页大小
     * @return 访客列表
     */
    VisitorListDto getVisitorListByVisiteeId(long visiteeId, long nextId, int limit);

    /**
     * 查询我看过谁（浏览历史），按 id DESC 游标分页。
     *
     * @param visitorId 访问者ID
     * @param nextId    游标：上一页最后一条的雪花ID
     * @param limit     分页大小
     * @return 浏览历史列表
     */
    VisitorListDto getHistoryListByVisitorId(long visitorId, long nextId, int limit);

    /**
     * 删除一条访客记录（主页主人删除某个访客）。
     *
     * @param visiteeId 被访问者ID
     * @param visitorId 访问者ID
     * @return 是否删除成功
     */
    boolean deleteVisitor(long visiteeId, long visitorId);

    /**
     * 删除一条浏览历史记录（访问者删除自己的浏览历史）。
     *
     * @param visitorId 访问者ID
     * @param visiteeId 被访问者ID
     * @return 是否删除成功
     */
    boolean deleteHistory(long visitorId, long visiteeId);

    /**
     * 批量清除新访客标记（用户查看访客列表后调用）。
     *
     * @param visiteeId 被访问者ID（主页主人）
     */
    void clearNewVisitors(long visiteeId);
}
