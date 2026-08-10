package com.clmcat.qianyu.live.room.support;

import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomCreateDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomDto;
import com.clmcat.qianyu.live.api.room.model.dto.LiveRoomListDto;
import com.clmcat.qianyu.live.api.room.model.dto.PushType;
import com.clmcat.qianyu.live.room.model.dto.LiveRoomCreateParamDto;
import com.clmcat.qianyu.live.room.model.entity.LiveRoom;
import com.clmcat.qianyu.live.room.model.entity.LiveRoomCount;
import com.clmcat.qianyu.live.room.model.vo.LiveRoomListVo;
import com.clmcat.qianyu.live.room.model.vo.LiveRoomVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 直播间工具支持类。
 * <p>
 * 提供 Entity ↔ Dto ↔ Vo 转换、默认 room_no 分配钩子、推流地址生成钩子。
 *
 * @author ark-home
 * @date 2026-07-08
 */
public class LiveRoomSupport {

    /**
     * 默认 room_no 分配策略：与内部 id 相同。
     * 预留钩子方法，后续可覆写为短号/靓号分配逻辑。
     *
     * @param id 内部雪花ID
     * @return 对外直播间编号
     */
    protected long allocateRoomNo(long id) {
        return id;
    }

    /**
     * 推流地址生成钩子。
     * <p>
     * 当前返回占位地址，接入 CDN 后覆盖此方法即可。无需修改调用方。
     *
     * @param roomNo   对外直播间编号
     * @param pushType 推流协议类型（服务端控制）
     * @return 推流地址
     */
    protected String generatePushUrl(long roomNo, PushType pushType) {
        // TODO: 接入 CDN 后替换为真实推流地址生成逻辑
        return pushType == PushType.RTMP
                ? "rtmp://push.example.com/live/" + roomNo
                : "https://webrtc.example.com/publish/" + roomNo;
    }

    // ---- 转换方法 ----

    /** LiveRoom + LiveRoomCount → LiveRoomDto */
    public static LiveRoomDto toDto(LiveRoom room, LiveRoomCount count) {
        if (room == null) {
            return null;
        }
        return LiveRoomDto.builder()
                .id(room.getId())
                .roomNo(room.getRoomNo())
                .anchorUserId(room.getAnchorUserId())
                .title(room.getTitle())
                .coverImage(room.getCoverImage())
                .status(room.getStatus())
                .viewerCount(count != null ? count.getViewerCount() : 0L)
                .likeCount(count != null ? count.getLikeCount() : 0L)
                .startTime(room.getStartTime())
                .endTime(room.getEndTime())
                .createTime(room.getCreateTime())
                .build();
    }

    /** LiveRoom（无计数信息） → LiveRoomDto */
    public static LiveRoomDto toDto(LiveRoom room) {
        return toDto(room, null);
    }

    /** List<LiveRoomDto> → LiveRoomListDto */
    public static LiveRoomListDto toListDto(List<LiveRoomDto> rooms, long nextNo, boolean hasMore) {
        if (rooms == null || rooms.isEmpty()) {
            return LiveRoomListDto.EMPTY;
        }
        return LiveRoomListDto.builder()
                .rooms(rooms)
                .nextNo(nextNo)
                .hasMore(hasMore)
                .build();
    }

    /** LiveRoomDto → LiveRoomVo */
    public static LiveRoomVo toVo(LiveRoomDto dto) {
        if (dto == null) {
            return null;
        }
        return LiveRoomVo.builder()
                .roomNo(dto.getRoomNo())
                .anchorUserId(dto.getAnchorUserId())
                .title(dto.getTitle())
                .coverImage(dto.getCoverImage())
                .status(dto.getStatus())
                .viewerCount(dto.getViewerCount())
                .likeCount(dto.getLikeCount())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    /** LiveRoomListDto → LiveRoomListVo */
    public static LiveRoomListVo toListVo(LiveRoomListDto dto) {
        if (dto == null || dto.getRooms() == null || dto.getRooms().isEmpty()) {
            return LiveRoomListVo.EMPTY;
        }
        List<LiveRoomVo> vos = dto.getRooms().stream()
                .map(LiveRoomSupport::toVo)
                .collect(Collectors.toList());
        return LiveRoomListVo.builder()
                .rooms(vos)
                .nextNo(dto.getNextNo())
                .hasMore(dto.getHasMore())
                .build();
    }

    /** Controller 层参数 → API 层参数 */
    public static LiveRoomCreateDto toApiCreateDto(LiveRoomCreateParamDto param) {
        if (param == null) {
            return null;
        }
        return LiveRoomCreateDto.builder()
                .title(trimToNull(param.getTitle()))
                .coverImage(trimToNull(param.getCoverImage()))
                .build();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
