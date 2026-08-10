package com.clmcat.qianyu.live.api.room;

import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomCreateDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomListDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveStartResult;

/**
 * 直播房间 RPC API。
 * <p>
 * 提供创建直播间、开播、关播、查询直播间信息和列表的能力。
 * 由 {@code qianyu-live-service} 中的 {@code LiveRoomServiceBiz} 通过 Dubbo 暴露。
 *
 * @author ark-home
 * @date 2026-07-08
 */
public interface LiveRoomApi {

    /**
     * 创建直播间。状态为"待开播"。
     *
     * @param anchorUserId 主播用户ID
     * @param dto          创建参数（标题、封面）
     * @return 创建好的直播间信息
     */
    LiveRoomDto createRoom(long anchorUserId, LiveRoomCreateDto dto);

    /**
     * 开播。将直播间状态从"待开播"切换为"直播中"，重置计数器，返回推流地址。
     * <p>
     * 推流协议类型由服务端控制，客户端无需传入。
     *
     * @param roomNo 对外直播间编号
     * @param userId 操作用户ID（须校验是否为主播本人）
     * @return 推流地址等开播结果
     */
    LiveStartResult startLive(long roomNo, long userId);

    /**
     * 关播。将直播间状态从"直播中"切换为"已结束"。
     *
     * @param roomNo 对外直播间编号
     * @param userId 操作用户ID（须校验是否为主播本人）
     */
    void closeLive(long roomNo, long userId);

    /**
     * 查询直播间详情（含计数器）。
     *
     * @param roomNo 对外直播间编号
     * @return 直播间信息，不存在返回 null
     */
    LiveRoomDto getRoomInfo(long roomNo);

    /**
     * 查询"直播中"的直播间列表（游标分页，按 roomNo 倒序）。
     *
     * @param nextNo 上一页最后一条的 roomNo，首次传 0
     * @param limit  每页条数
     * @return 直播间列表
     */
    LiveRoomListDto getLiveRoomList(long nextNo, int limit);

    /**
     * 查询当前用户的直播间（取最近创建的一条）。
     *
     * @param anchorUserId 主播用户ID
     * @return 直播间信息，不存在返回 null
     */
    LiveRoomDto getMyLiveRoom(long anchorUserId);

    /**
     * 更新对外直播间编号（room_no）。
     * 仅"待开播"状态允许修改。默认分配策略为 room_no == id，
     * 预留钩子 {@code allocateRoomNo(id)} 供后续扩展（短号/靓号分配）。
     *
     * @param roomNo    当前对外直播间编号
     * @param newRoomNo 新的对外直播间编号
     * @param userId    操作用户ID（须校验是否为主播本人）
     */
    void updateRoomNo(long roomNo, long newRoomNo, long userId);
}
