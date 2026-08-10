package com.clmcat.qianyu.live.room.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.live.api.room.model.dto.LiveStartResult;
import com.clmcat.qianyu.live.room.model.dto.LiveRoomCreateParamDto;
import com.clmcat.qianyu.live.room.model.dto.LiveRoomIdDto;
import com.clmcat.qianyu.live.room.model.dto.LiveRoomQueryDto;
import com.clmcat.qianyu.live.room.model.dto.UpdateRoomIdDto;
import com.clmcat.qianyu.live.room.model.vo.LiveRoomListVo;
import com.clmcat.qianyu.live.room.model.vo.LiveRoomVo;
import com.clmcat.qianyu.live.room.service.LiveRoomViewServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 直播房间接口。
 * <p>
 * 提供创建直播间、开播、关播、查询直播间详情/列表等 HTTP API。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Tag(name = "直播房间接口", description = "提供直播间创建、开播、关播、详情查询和列表查询能力。")
@ApiController
@RequestMapping("/api/live/room")
public class LiveRoomController {

    @Resource
    private LiveRoomViewServiceBiz liveRoomViewServiceBiz;

    @Operation(summary = "创建直播间")
    @PostMapping("/create")
    @LoginVerify
    public LiveRoomVo create(@Parameter(hidden = true) @Token long userId,
                             @Params(description = "创建直播间参数") LiveRoomCreateParamDto param) {
        return liveRoomViewServiceBiz.createRoom(userId, param);
    }

    @Operation(summary = "开播（返回推流地址）")
    @PostMapping("/start")
    @LoginVerify
    public LiveStartResult start(@Parameter(hidden = true) @Token long userId,
                                  @Params(description = "直播间编号") LiveRoomIdDto dto) {
        return liveRoomViewServiceBiz.startLive(userId, dto.getRoomNo());
    }

    @Operation(summary = "关播")
    @PostMapping("/close")
    @LoginVerify
    public void close(@Parameter(hidden = true) @Token long userId,
                      @Params(description = "直播间编号") LiveRoomIdDto dto) {
        liveRoomViewServiceBiz.closeLive(userId, dto.getRoomNo());
    }

    @Operation(summary = "直播间详情（含计数）")
    @GetMapping("/info")
    public LiveRoomVo info(@ParameterObject @Params LiveRoomIdDto dto) {
        return liveRoomViewServiceBiz.getRoomInfo(dto.getRoomNo());
    }

    @Operation(summary = "直播中列表")
    @GetMapping("/list")
    public LiveRoomListVo list(@ParameterObject @Params LiveRoomQueryDto dto) {
        return liveRoomViewServiceBiz.getLiveRoomList(dto);
    }

    @Operation(summary = "我的直播间")
    @GetMapping("/my")
    @LoginVerify
    public LiveRoomVo my(@Parameter(hidden = true) @Token long userId) {
        return liveRoomViewServiceBiz.getMyLiveRoom(userId);
    }

    @Operation(summary = "更新直播间编号（仅待开播状态）")
    @PostMapping("/update_room_no")
    @LoginVerify
    public void updateRoomNo(@Parameter(hidden = true) @Token long userId,
                             @Params(description = "直播间编号更新参数") UpdateRoomIdDto dto) {
        liveRoomViewServiceBiz.updateRoomNo(userId, dto);
    }
}
