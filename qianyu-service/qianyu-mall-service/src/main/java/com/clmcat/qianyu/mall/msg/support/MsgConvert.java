package com.clmcat.qianyu.mall.msg.support;

import com.clmcat.qianyu.mall.msg.model.entity.MsgMessage;
import com.clmcat.qianyu.mall.msg.model.vo.MsgVO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** {@link MsgMessage} → {@link MsgVO} 转换（含时间格式化）。照 FavConvert 范式。 */
public class MsgConvert {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static MsgVO toVO(MsgMessage m) {
        if (m == null) {
            return null;
        }
        return MsgVO.builder()
                .messageId(m.getId())
                .type(m.getType())
                .title(m.getTitle())
                .content(m.getContent())
                .bizType(m.getBizType())
                .bizId(m.getBizId())
                .isRead(m.getIsRead())
                .createTime(m.getCreateTime())
                .createTimeText(formatTime(m.getCreateTime()))
                .build();
    }

    public static String formatTime(Long millis) {
        if (millis == null || millis <= 0) {
            return "";
        }
        return FMT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()));
    }
}
