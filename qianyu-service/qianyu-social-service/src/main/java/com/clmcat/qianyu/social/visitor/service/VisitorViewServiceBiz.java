package com.clmcat.qianyu.social.visitor.service;

import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorDto;
import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorListDto;
import com.clmcat.qianyu.social.visitor.model.dto.VisitorListQueryDto;
import com.clmcat.qianyu.social.visitor.model.dto.VisitorSelfListQueryDto;
import com.clmcat.qianyu.social.visitor.model.entity.status.Status;
import com.clmcat.qianyu.social.visitor.model.vo.VisitorCountVo;
import com.clmcat.qianyu.social.visitor.model.vo.VisitorPageVo;
import com.clmcat.qianyu.social.visitor.model.vo.VisitorUserVo;
import com.clmcat.qianyu.social.visitor.support.VisitorSupport;
import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.PpcUserInfoListDto;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 访客查询视图服务（VO 输出 + 用户信息批量填充）。
 */
@Service
public class VisitorViewServiceBiz {

    @Resource
    VisitorServiceBiz visitorServiceBiz;

    @Resource
    UserApi userApi;

    /**
     * 记录一次主页访问。
     *
     * @param visitorId 访问者ID（当前登录用户）
     * @param targetId  被访问者ID（主页主人）
     */
    public void recordVisit(long visitorId, long targetId) {
        Status.VISIT_SELF_NOT_ALLOWED.assertThrowResEx(visitorId == targetId);
        VisitorDto dto = VisitorDto.builder()
                .visitorId(visitorId)
                .visiteeId(targetId)
                .build();
        visitorServiceBiz.recordVisit(dto);
    }

    /**
     * 查询某个用户的访客列表（谁看过 ta）。
     */
    public VisitorPageVo getVisitorList(VisitorListQueryDto dto) {
        long userId = dto == null ? 0L : Objects.requireNonNullElse(dto.getUserId(), 0L);
        Status.USER_REQUIRED.assertThrowResEx(VisitorSupport.isNullOrNonPositive(userId));

        int limit = VisitorSupport.normalizeLimit(dto == null ? null : dto.getLimit());
        long nextId = VisitorSupport.normalizeCursorId(dto == null ? null : dto.getNextId());
        List<VisitorDto> visitorDtos = visitorServiceBiz.getVisitorListByVisiteeId(userId, nextId, limit + 1).getVisitors();

        boolean hasMore = visitorDtos.size() > limit;
        if (hasMore) {
            visitorDtos = new ArrayList<>(visitorDtos.subList(0, limit));
        }
        long nextCursorId = hasMore && !visitorDtos.isEmpty() ? visitorDtos.get(visitorDtos.size() - 1).getId() : 0L;
        Map<Long, RpcUserInfoDto> userMap = queryUserInfoMapByVisitorIds(visitorDtos);
        List<VisitorUserVo> userList = VisitorSupport.toVisitorUserVoList(visitorDtos, userMap);
        return VisitorSupport.newVisitorPageVo(userId, hasMore, nextCursorId, userList);
    }

    /**
     * 查询当前登录用户自己的访客列表。
     */
    public VisitorPageVo getSelfVisitorList(long userId, VisitorSelfListQueryDto dto) {
        Status.USER_REQUIRED.assertThrowResEx(VisitorSupport.isNullOrNonPositive(userId));
        return getVisitorList(newSelfListQuery(userId, dto));
    }

    /**
     * 查询某个用户的浏览历史（ta 看过谁）。
     */
    public VisitorPageVo getHistoryList(VisitorListQueryDto dto) {
        long userId = dto == null ? 0L : Objects.requireNonNullElse(dto.getUserId(), 0L);
        Status.USER_REQUIRED.assertThrowResEx(VisitorSupport.isNullOrNonPositive(userId));

        int limit = VisitorSupport.normalizeLimit(dto == null ? null : dto.getLimit());
        long nextId = VisitorSupport.normalizeCursorId(dto == null ? null : dto.getNextId());
        List<VisitorDto> historyDtos = visitorServiceBiz.getHistoryListByVisitorId(userId, nextId, limit + 1).getVisitors();

        boolean hasMore = historyDtos.size() > limit;
        if (hasMore) {
            historyDtos = new ArrayList<>(historyDtos.subList(0, limit));
        }
        long nextCursorId = hasMore && !historyDtos.isEmpty() ? historyDtos.get(historyDtos.size() - 1).getId() : 0L;
        Map<Long, RpcUserInfoDto> userMap = queryUserInfoMapByVisiteeIds(historyDtos);
        List<VisitorUserVo> userList = VisitorSupport.toHistoryUserVoList(historyDtos, userMap);
        return VisitorSupport.newVisitorPageVo(userId, hasMore, nextCursorId, userList);
    }

    /**
     * 查询当前登录用户自己的浏览历史。
     */
    public VisitorPageVo getSelfHistoryList(long userId, VisitorSelfListQueryDto dto) {
        Status.USER_REQUIRED.assertThrowResEx(VisitorSupport.isNullOrNonPositive(userId));
        return getHistoryList(newSelfListQuery(userId, dto));
    }

    /**
     * 删除一条访客记录。
     *
     * @param visiteeId 被访问者ID（当前登录用户）
     * @param visitorId 要删除的访问者ID
     */
    public boolean deleteVisitor(long visiteeId, long visitorId) {
        Status.USER_REQUIRED.assertThrowResEx(VisitorSupport.isNullOrNonPositive(visiteeId)
                || VisitorSupport.isNullOrNonPositive(visitorId));
        return visitorServiceBiz.deleteVisitor(visiteeId, visitorId);
    }

    /**
     * 删除一条浏览历史记录。
     *
     * @param visitorId 访问者ID（当前登录用户）
     * @param visiteeId 要删除的被访问者ID
     */
    public boolean deleteHistory(long visitorId, long visiteeId) {
        Status.USER_REQUIRED.assertThrowResEx(VisitorSupport.isNullOrNonPositive(visitorId)
                || VisitorSupport.isNullOrNonPositive(visiteeId));
        return visitorServiceBiz.deleteHistory(visitorId, visiteeId);
    }

    /**
     * 查询某个用户的访客数量。
     */
    public VisitorCountVo getVisitorCount(long userId) {
        Status.USER_REQUIRED.assertThrowResEx(VisitorSupport.isNullOrNonPositive(userId));
        return VisitorSupport.toVisitorCountVo(userId,
                visitorServiceBiz.getVisitorCount(userId).getVisitorCount());
    }

    /**
     * 查询当前登录用户自己的访客数量。
     */
    public VisitorCountVo getSelfVisitorCount(long userId) {
        Status.USER_REQUIRED.assertThrowResEx(VisitorSupport.isNullOrNonPositive(userId));
        return getVisitorCount(userId);
    }

    /**
     * 清除当前用户的新访客标记。
     */
    public void clearNewVisitors(long userId) {
        Status.USER_REQUIRED.assertThrowResEx(VisitorSupport.isNullOrNonPositive(userId));
        visitorServiceBiz.clearNewVisitors(userId);
    }

    // ========== 辅助方法 ==========

    private VisitorListQueryDto newSelfListQuery(long userId, VisitorSelfListQueryDto dto) {
        VisitorListQueryDto queryDto = new VisitorListQueryDto();
        queryDto.setUserId(userId);
        if (dto != null) {
            queryDto.setNextId(dto.getNextId());
            queryDto.setLimit(dto.getLimit());
        }
        return queryDto;
    }

    /**
     * 批量查询访客列表中的用户信息（谁来看的）。
     */
    private Map<Long, RpcUserInfoDto> queryUserInfoMapByVisitorIds(List<VisitorDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = dtos.stream()
                .map(VisitorDto::getVisitorId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        return queryUserInfoMap(userIds);
    }

    /**
     * 批量查询浏览历史列表中的用户信息（我去看了谁）。
     */
    private Map<Long, RpcUserInfoDto> queryUserInfoMapByVisiteeIds(List<VisitorDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = dtos.stream()
                .map(VisitorDto::getVisiteeId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        return queryUserInfoMap(userIds);
    }

    /**
     * 通过 UserApi 批量查询用户信息（userId → nickname/avatar）。
     */
    private Map<Long, RpcUserInfoDto> queryUserInfoMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            PpcUserInfoListDto userList = userApi.getUserInfoList(userIds);
            if (userList == null || userList.getUsers() == null) {
                return Collections.emptyMap();
            }
            Map<Long, RpcUserInfoDto> map = new HashMap<>(userList.getUsers().size());
            for (RpcUserInfoDto user : userList.getUsers()) {
                if (user != null && user.getUserId() != null) {
                    map.put(user.getUserId(), user);
                }
            }
            return map;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
