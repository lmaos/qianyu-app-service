package com.clmcat.qianyu.live.room.service;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomCreateDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomListDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveStartResult;
import com.clmcat.qianyu.live.room.model.dto.LiveRoomCreateParamDto;
import com.clmcat.qianyu.live.room.model.dto.LiveRoomQueryDto;
import com.clmcat.qianyu.live.room.model.dto.UpdateRoomIdDto;
import com.clmcat.qianyu.live.room.model.vo.LiveRoomListVo;
import com.clmcat.qianyu.live.room.model.vo.LiveRoomVo;
import com.clmcat.qianyu.live.room.support.LiveRoomSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 直播房间视图/聚合服务。
 * <p>
 * 供 Controller 层调用，负责参数校验、VO 组装，以及后续需要聚合主播用户信息等跨模块调用。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Service
public class LiveRoomViewServiceBiz {

    @Resource
    private LiveRoomServiceBiz liveRoomServiceBiz;

    /** 创建直播间 */
    public LiveRoomVo createRoom(long userId, LiveRoomCreateParamDto param) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(param == null || param.getTitle() == null || param.getTitle().trim().isEmpty());
        LiveRoomCreateDto apiDto = LiveRoomSupport.toApiCreateDto(param);
        LiveRoomDto result = liveRoomServiceBiz.createRoom(userId, apiDto);
        return LiveRoomSupport.toVo(result);
    }

    /** 开播，返回推流地址等开播结果 */
    public LiveStartResult startLive(long userId, long roomNo) {
        return liveRoomServiceBiz.startLive(roomNo, userId);
    }

    /** 关播 */
    public void closeLive(long userId, long roomNo) {
        liveRoomServiceBiz.closeLive(roomNo, userId);
    }

    /** 直播间详情 */
    public LiveRoomVo getRoomInfo(long roomNo) {
        LiveRoomDto dto = liveRoomServiceBiz.getRoomInfo(roomNo);
        return LiveRoomSupport.toVo(dto);
    }

    /** 直播中列表 */
    public LiveRoomListVo getLiveRoomList(LiveRoomQueryDto query) {
        long nextNo = query != null && query.getNextNo() != null ? query.getNextNo() : 0L;
        int limit = query != null && query.getLimit() != null ? query.getLimit() : 20;
        LiveRoomListDto dto = liveRoomServiceBiz.getLiveRoomList(nextNo, limit);
        return LiveRoomSupport.toListVo(dto);
    }

    /** 我的直播间 */
    public LiveRoomVo getMyLiveRoom(long userId) {
        LiveRoomDto dto = liveRoomServiceBiz.getMyLiveRoom(userId);
        return LiveRoomSupport.toVo(dto);
    }

    /** 更新对外直播间编号 */
    public void updateRoomNo(long userId, UpdateRoomIdDto dto) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto == null || dto.getRoomNo() == null || dto.getNewRoomNo() == null);
        liveRoomServiceBiz.updateRoomNo(dto.getRoomNo(), dto.getNewRoomNo(), userId);
    }
}
