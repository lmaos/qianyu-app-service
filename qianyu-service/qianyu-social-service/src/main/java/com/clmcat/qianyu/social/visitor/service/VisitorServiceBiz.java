package com.clmcat.qianyu.social.visitor.service;

import com.clmcat.qianyu.social.api.visitor.VisitorApi;
import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorCountDto;
import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorDto;
import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorListDto;
import com.clmcat.qianyu.social.visitor.mapper.UserHistoryMapper;
import com.clmcat.qianyu.social.visitor.mapper.UserVisitorMapper;
import com.clmcat.qianyu.social.visitor.model.entity.UserHistory;
import com.clmcat.qianyu.social.visitor.model.entity.UserVisitor;
import com.clmcat.qianyu.social.visitor.model.entity.status.Status;
import com.clmcat.qianyu.social.visitor.support.VisitorSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@DubboService
public class VisitorServiceBiz implements VisitorApi {

    @Resource
    UserVisitorMapper userVisitorMapper;

    @Resource
    UserHistoryMapper userHistoryMapper;

    /**
     * 记录一次主页访问（双写 user_visitor + user_history）。
     * <p>
     * 使用 INSERT ... ON DUPLICATE KEY UPDATE 实现 upsert 语义：
     * 首次访问插入新记录，再次访问更新 visit_count、is_new、时间。
     */
    @Override
    @Transactional
    public void recordVisit(VisitorDto dto) {
        verify(dto);
        long id = VisitorSupport.VISITOR_ID_SNOWFLAKE.nextId();
        long clientTime = VisitorSupport.parseClientTime(id);

        // 写入访客记录
        UserVisitor visitor = new UserVisitor();
        visitor.setId(id);
        visitor.setVisitorId(dto.getVisitorId());
        visitor.setVisiteeId(dto.getVisiteeId());
        visitor.setClientTime(clientTime);

        // 写入浏览历史
        UserHistory history = new UserHistory();
        history.setId(id);
        history.setVisitorId(dto.getVisitorId());
        history.setVisiteeId(dto.getVisiteeId());
        history.setClientTime(clientTime);

        try {
            userVisitorMapper.upsert(visitor);
            userHistoryMapper.upsert(history);
        } catch (DuplicateKeyException e) {
            // 极小概率的并发冲突，忽略即可
            log.debug("recordVisit upsert conflict, visitorId={}, visiteeId={}", dto.getVisitorId(), dto.getVisiteeId());
        }
    }

    /**
     * 查询用户的新访客数（is_new = 1 的记录数）。
     */
    @Override
    public VisitorCountDto getVisitorCount(long userId) {
        Status.USER_REQUIRED.assertThrowResEx(userId <= 0);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(UserVisitor::getVisiteeId, userId)
                .eq(UserVisitor::getIsNew, VisitorSupport.IS_NEW_YES);
        long count = userVisitorMapper.selectCountByQuery(queryWrapper);

        return VisitorCountDto.builder()
                .userId(userId)
                .visitorCount(count)
                .build();
    }

    /**
     * 查询谁看过我（访客列表），按 id DESC 游标分页。
     */
    @Override
    public VisitorListDto getVisitorListByVisiteeId(long visiteeId, long nextId, int limit) {
        if (visiteeId <= 0 || limit <= 0) {
            return VisitorListDto.EMPTY;
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(UserVisitor::getVisiteeId, visiteeId);
        queryWrapper.lt(UserVisitor::getId, nextId);
        queryWrapper.orderBy(UserVisitor::getId, false);
        queryWrapper.limit(limit);
        List<UserVisitor> entities = userVisitorMapper.selectListByQuery(queryWrapper);
        List<VisitorDto> list = VisitorSupport.toVisitorDtoListFromVisitor(entities);
        return VisitorListDto.builder().visitors(list).build();
    }

    /**
     * 查询我看过谁（浏览历史），按 id DESC 游标分页。
     */
    @Override
    public VisitorListDto getHistoryListByVisitorId(long visitorId, long nextId, int limit) {
        if (visitorId <= 0 || limit <= 0) {
            return VisitorListDto.EMPTY;
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(UserHistory::getVisitorId, visitorId);
        queryWrapper.lt(UserHistory::getId, nextId);
        queryWrapper.orderBy(UserHistory::getId, false);
        queryWrapper.limit(limit);
        List<UserHistory> entities = userHistoryMapper.selectListByQuery(queryWrapper);
        List<VisitorDto> list = VisitorSupport.toVisitorDtoListFromHistory(entities);
        return VisitorListDto.builder().visitors(list).build();
    }

    /**
     * 删除一条访客记录（主页主人删除某个访客）。
     */
    @Override
    public boolean deleteVisitor(long visiteeId, long visitorId) {
        if (visiteeId <= 0 || visitorId <= 0) {
            return false;
        }
        int rows = userVisitorMapper.deleteByVisiteeAndVisitor(visiteeId, visitorId);
        return rows > 0;
    }

    /**
     * 删除一条浏览历史记录（访问者删除自己的浏览历史）。
     */
    @Override
    public boolean deleteHistory(long visitorId, long visiteeId) {
        if (visitorId <= 0 || visiteeId <= 0) {
            return false;
        }
        int rows = userHistoryMapper.deleteByVisitorAndVisitee(visitorId, visiteeId);
        return rows > 0;
    }

    /**
     * 批量清除新访客标记（用户查看访客列表后调用）。
     */
    @Override
    public void clearNewVisitors(long visiteeId) {
        if (visiteeId <= 0) {
            return;
        }
        int rows = userVisitorMapper.clearNewFlag(visiteeId);
        log.debug("clearNewVisitors: visiteeId={}, cleared {} rows", visiteeId, rows);
    }

    private void verify(VisitorDto dto) {
        Status.USER_REQUIRED.assertThrowResEx(dto == null
                || VisitorSupport.isNullOrNonPositive(dto.getVisitorId())
                || VisitorSupport.isNullOrNonPositive(dto.getVisiteeId()));
        Status.VISIT_SELF_NOT_ALLOWED.assertThrowResEx(dto.getVisitorId().equals(dto.getVisiteeId()));
    }
}
