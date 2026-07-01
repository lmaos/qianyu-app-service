package com.clmcat.qianyu.social.visitor.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorDto;
import com.clmcat.qianyu.social.visitor.model.entity.UserHistory;
import com.clmcat.qianyu.social.visitor.model.entity.UserVisitor;
import com.clmcat.qianyu.social.visitor.model.vo.VisitorCountVo;
import com.clmcat.qianyu.social.visitor.model.vo.VisitorPageVo;
import com.clmcat.qianyu.social.visitor.model.vo.VisitorUserVo;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class VisitorSupport {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;
    public static final int IS_NEW_YES = 1;
    public static final int IS_NEW_NO = 0;

    public static final CustomSnowflake VISITOR_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    // ========== Entity → DTO ==========

    public static VisitorDto toVisitorDto(UserVisitor e) {
        if (e == null) {
            return null;
        }
        return VisitorDto.builder()
                .id(e.getId())
                .visitorId(e.getVisitorId())
                .visiteeId(e.getVisiteeId())
                .visitCount(e.getVisitCount())
                .isNew(e.getIsNew())
                .clientTime(e.getClientTime())
                .build();
    }

    public static VisitorDto toVisitorDto(UserHistory e) {
        if (e == null) {
            return null;
        }
        return VisitorDto.builder()
                .id(e.getId())
                .visitorId(e.getVisitorId())
                .visiteeId(e.getVisiteeId())
                .visitCount(e.getVisitCount())
                .clientTime(e.getClientTime())
                .build();
    }

    public static List<VisitorDto> toVisitorDtoListFromVisitor(Collection<UserVisitor> entities) {
        List<VisitorDto> list = new ArrayList<>();
        if (entities == null) {
            return list;
        }
        for (UserVisitor e : entities) {
            VisitorDto dto = toVisitorDto(e);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

    public static List<VisitorDto> toVisitorDtoListFromHistory(Collection<UserHistory> entities) {
        List<VisitorDto> list = new ArrayList<>();
        if (entities == null) {
            return list;
        }
        for (UserHistory e : entities) {
            VisitorDto dto = toVisitorDto(e);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

    // ========== DTO → VO（含用户信息） ==========

    /**
     * 将访客列表 DTO 转为 VO（对方是 visitor，即"谁来看我的"）。
     */
    public static VisitorUserVo toVisitorUserVo(VisitorDto dto, Map<Long, RpcUserInfoDto> userMap) {
        if (dto == null) {
            return null;
        }
        RpcUserInfoDto userInfo = userMap != null ? userMap.get(dto.getVisitorId()) : null;
        return VisitorUserVo.builder()
                .id(dto.getId())
                .userId(dto.getVisitorId())
                .visitCount(dto.getVisitCount())
                .clientTime(dto.getClientTime())
                .nickname(userInfo != null ? userInfo.getNickname() : null)
                .avatar(userInfo != null ? userInfo.getAvatar() : null)
                .build();
    }

    /**
     * 将浏览历史列表 DTO 转为 VO（对方是 visitee，即"我去看了谁"）。
     */
    public static VisitorUserVo toHistoryUserVo(VisitorDto dto, Map<Long, RpcUserInfoDto> userMap) {
        if (dto == null) {
            return null;
        }
        RpcUserInfoDto userInfo = userMap != null ? userMap.get(dto.getVisiteeId()) : null;
        return VisitorUserVo.builder()
                .id(dto.getId())
                .userId(dto.getVisiteeId())
                .visitCount(dto.getVisitCount())
                .clientTime(dto.getClientTime())
                .nickname(userInfo != null ? userInfo.getNickname() : null)
                .avatar(userInfo != null ? userInfo.getAvatar() : null)
                .build();
    }

    public static List<VisitorUserVo> toVisitorUserVoList(Collection<VisitorDto> dtos, Map<Long, RpcUserInfoDto> userMap) {
        List<VisitorUserVo> list = new ArrayList<>();
        if (dtos == null) {
            return list;
        }
        for (VisitorDto dto : dtos) {
            VisitorUserVo vo = toVisitorUserVo(dto, userMap);
            if (vo != null) {
                list.add(vo);
            }
        }
        return list;
    }

    public static List<VisitorUserVo> toHistoryUserVoList(Collection<VisitorDto> dtos, Map<Long, RpcUserInfoDto> userMap) {
        List<VisitorUserVo> list = new ArrayList<>();
        if (dtos == null) {
            return list;
        }
        for (VisitorDto dto : dtos) {
            VisitorUserVo vo = toHistoryUserVo(dto, userMap);
            if (vo != null) {
                list.add(vo);
            }
        }
        return list;
    }

    // ========== Page / Count VO ==========

    public static VisitorPageVo newVisitorPageVo(long userId, boolean hasMore, long nextId,
                                                  List<VisitorUserVo> userList) {
        return VisitorPageVo.builder()
                .userId(userId)
                .hasMore(hasMore)
                .nextId(nextId)
                .userList(userList)
                .build();
    }

    public static VisitorCountVo toVisitorCountVo(long userId, long visitorCount) {
        return VisitorCountVo.builder()
                .userId(userId)
                .visitorCount(visitorCount)
                .build();
    }

    // ========== 工具方法 ==========

    public static int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /**
     * 标准化游标 ID（没有传时用 Long.MAX_VALUE 表示从最新开始）。
     */
    public static long normalizeCursorId(Long nextId) {
        if (isNullOrNonPositive(nextId)) {
            return Long.MAX_VALUE;
        }
        return nextId;
    }

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.longValue() <= 0;
    }

    /**
     * 解析雪花 ID 中的客户端时间戳。
     */
    public static long parseClientTime(long id) {
        return SnowflakeSupport.parseTimeBySnowflake(VISITOR_ID_SNOWFLAKE, id);
    }
}
