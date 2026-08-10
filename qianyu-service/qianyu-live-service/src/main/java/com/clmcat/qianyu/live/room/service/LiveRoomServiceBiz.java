package com.clmcat.qianyu.live.room.service;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.live.api.room.LiveRoomApi;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomCreateDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomListDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveStartResult;
import com.clmcat.qianyu.live.api.room.model.dto.PushType;
import com.clmcat.qianyu.live.room.mapper.LiveRoomCountMapper;
import com.clmcat.qianyu.live.room.mapper.LiveRoomMapper;
import com.clmcat.qianyu.live.room.model.entity.LiveRoom;
import com.clmcat.qianyu.live.room.model.entity.LiveRoomCount;
import com.clmcat.qianyu.live.room.support.LiveRoomSupport;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播房间核心业务服务。
 * <p>
 * 实现 {@link LiveRoomApi}，通过 Dubbo RPC 暴露。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@DubboService
@Service
public class LiveRoomServiceBiz extends LiveRoomSupport implements LiveRoomApi {

    private static final CustomSnowflake ROOM_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    private static final int MAX_LIMIT = 50;

    @Resource
    private LiveRoomMapper liveRoomMapper;

    @Resource
    private LiveRoomCountMapper liveRoomCountMapper;

    @Override
    @Transactional
    public LiveRoomDto createRoom(long anchorUserId, LiveRoomCreateDto dto) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(anchorUserId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto == null || dto.getTitle() == null || dto.getTitle().trim().isEmpty());

        long now = System.currentTimeMillis();
        long id = ROOM_ID_SNOWFLAKE.nextId();
        long roomNo = allocateRoomNo(id);

        LiveRoom room = LiveRoom.builder()
                .id(id)
                .roomNo(roomNo)
                .anchorUserId(anchorUserId)
                .title(dto.getTitle().trim())
                .coverImage(defaultEmpty(dto.getCoverImage()))
                .status(LiveRoom.STATUS_PENDING)
                .startTime(0L)
                .endTime(0L)
                .createTime(now)
                .updateTime(now)
                .build();
        liveRoomMapper.customInsert(room);

        liveRoomCountMapper.customInsert(id);

        return toDto(room);
    }

    @Override
    @Transactional
    public LiveStartResult startLive(long roomNo, long userId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(roomNo <= 0 || userId <= 0);

        LiveRoom room = liveRoomMapper.customSelectByRoomNo(roomNo);
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx(room == null, "直播间不存在");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(!room.getAnchorUserId().equals(userId), "非主播本人不可开播");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(room.getStatus() != LiveRoom.STATUS_PENDING, "仅待开播状态可开播");

        long now = System.currentTimeMillis();
        room.setStatus(LiveRoom.STATUS_LIVE);
        room.setStartTime(now);
        room.setUpdateTime(now);
        liveRoomMapper.customUpdateById(room);

        liveRoomCountMapper.customResetCount(room.getId());

        PushType pushType = PushType.RTMP;
        String pushUrl = generatePushUrl(roomNo, pushType);

        return LiveStartResult.builder()
                .roomNo(roomNo)
                .pushType(pushType)
                .pushUrl(pushUrl)
                .startTime(now)
                .build();
    }

    @Override
    @Transactional
    public void closeLive(long roomNo, long userId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(roomNo <= 0 || userId <= 0);

        LiveRoom room = liveRoomMapper.customSelectByRoomNo(roomNo);
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx(room == null, "直播间不存在");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(!room.getAnchorUserId().equals(userId), "非主播本人不可关播");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(room.getStatus() != LiveRoom.STATUS_LIVE, "仅直播中状态可关播");

        long now = System.currentTimeMillis();
        room.setStatus(LiveRoom.STATUS_CLOSED);
        room.setEndTime(now);
        room.setUpdateTime(now);
        liveRoomMapper.customUpdateById(room);
    }

    @Override
    public LiveRoomDto getRoomInfo(long roomNo) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(roomNo <= 0);

        LiveRoom room = liveRoomMapper.customSelectByRoomNo(roomNo);
        if (room == null) {
            return null;
        }
        LiveRoomCount count = liveRoomCountMapper.customSelectByRoomId(room.getId());
        return toDto(room, count);
    }

    @Override
    public LiveRoomListDto getLiveRoomList(long nextNo, int limit) {
        if (limit <= 0 || limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }

        List<LiveRoom> rooms = liveRoomMapper.customSelectLiveList(nextNo, limit + 1);
        if (rooms == null || rooms.isEmpty()) {
            return LiveRoomListDto.EMPTY;
        }

        boolean hasMore = rooms.size() > limit;
        if (hasMore) {
            rooms = rooms.subList(0, limit);
        }

        List<LiveRoomDto> dtoList = new ArrayList<>();
        for (LiveRoom room : rooms) {
            LiveRoomCount count = liveRoomCountMapper.customSelectByRoomId(room.getId());
            dtoList.add(toDto(room, count));
        }

        long nextNoResult = hasMore && !rooms.isEmpty() ? rooms.get(rooms.size() - 1).getRoomNo() : 0L;
        return toListDto(dtoList, nextNoResult, hasMore);
    }

    @Override
    public LiveRoomDto getMyLiveRoom(long anchorUserId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(anchorUserId <= 0);

        LiveRoom room = liveRoomMapper.customSelectLatestByAnchorUserId(anchorUserId);
        if (room == null) {
            return null;
        }
        LiveRoomCount count = liveRoomCountMapper.customSelectByRoomId(room.getId());
        return toDto(room, count);
    }

    @Override
    @Transactional
    public void updateRoomNo(long roomNo, long newRoomNo, long userId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(roomNo <= 0 || newRoomNo <= 0 || userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(roomNo == newRoomNo, "新旧编号不能相同");

        LiveRoom room = liveRoomMapper.customSelectByRoomNo(roomNo);
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx(room == null, "直播间不存在");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(!room.getAnchorUserId().equals(userId), "非主播本人不可修改");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(room.getStatus() != LiveRoom.STATUS_PENDING, "仅待开播状态可修改编号");

        long now = System.currentTimeMillis();
        int affected = liveRoomMapper.customUpdateRoomNo(room.getId(), newRoomNo, now);
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx(affected <= 0, "编号更新失败（可能已被占用）");
    }

    private static String defaultEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
