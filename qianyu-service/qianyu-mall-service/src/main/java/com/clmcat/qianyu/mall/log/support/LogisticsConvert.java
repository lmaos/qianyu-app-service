package com.clmcat.qianyu.mall.log.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.log.model.entity.LogDeliveryTrace;
import com.clmcat.qianyu.mall.log.model.entity.LogShipping;
import com.clmcat.qianyu.mall.log.model.vo.LogisticsDetailVO;
import com.clmcat.qianyu.mall.log.model.vo.LogisticsTraceVO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogisticsConvert {

    public static final CustomSnowflake LOG_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<Integer, Integer> SQL_TO_API_STATUS = new HashMap<>();
    private static final Map<Integer, String> STATUS_TEXT_MAP = new HashMap<>();

    static {
        // SQL status -> API status mapping
        SQL_TO_API_STATUS.put(0, 2);  // 已发货 -> 运输中
        SQL_TO_API_STATUS.put(1, 2);  // 运输中 -> 运输中
        SQL_TO_API_STATUS.put(2, 4);  // 已签收 -> 已签收
        SQL_TO_API_STATUS.put(3, 5);  // 异常 -> 异常

        // API status -> text
        STATUS_TEXT_MAP.put(1, "已揽收");
        STATUS_TEXT_MAP.put(2, "运输中");
        STATUS_TEXT_MAP.put(3, "派送中");
        STATUS_TEXT_MAP.put(4, "已签收");
        STATUS_TEXT_MAP.put(5, "异常");
    }

    /**
     * SQL 物流状态转 API 状态
     */
    public static int sqlStatusToApiStatus(Integer sqlStatus) {
        if (sqlStatus == null) {
            return 2;
        }
        return SQL_TO_API_STATUS.getOrDefault(sqlStatus, 2);
    }

    /**
     * API 状态转中文描述
     */
    public static String statusText(int apiStatus) {
        return STATUS_TEXT_MAP.getOrDefault(apiStatus, "未知");
    }

    /**
     * 毫秒时间戳转格式化字符串
     */
    public static String formatTime(Long millis) {
        if (millis == null || millis <= 0) {
            return "";
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        return ldt.format(FORMATTER);
    }

    /**
     * LogDeliveryTrace 转 LogisticsTraceVO
     */
    public static LogisticsTraceVO toTraceVO(LogDeliveryTrace trace) {
        if (trace == null) {
            return null;
        }
        return LogisticsTraceVO.builder()
                .time(formatTime(trace.getTraceTime()))
                .content(trace.getDescription())
                .location(trace.getLocation())
                .build();
    }

    /**
     * 批量转换轨迹
     */
    public static List<LogisticsTraceVO> toTraceVOList(List<LogDeliveryTrace> traces) {
        List<LogisticsTraceVO> voList = new ArrayList<>();
        if (traces == null) {
            return voList;
        }
        for (LogDeliveryTrace trace : traces) {
            LogisticsTraceVO vo = toTraceVO(trace);
            if (vo != null) {
                voList.add(vo);
            }
        }
        return voList;
    }

    /**
     * 组装物流详情 VO
     */
    public static LogisticsDetailVO toDetailVO(LogShipping shipping, List<LogDeliveryTrace> traces) {
        if (shipping == null) {
            return null;
        }
        int apiStatus = sqlStatusToApiStatus(shipping.getStatus());
        return LogisticsDetailVO.builder()
                .logisticsId(shipping.getId())
                .logisticsCompany(shipping.getShippingCompanyName())
                .logisticsCode(shipping.getShippingCompany())
                .logisticsNo(shipping.getShippingNo())
                .status(apiStatus)
                .statusText(statusText(apiStatus))
                .traces(toTraceVOList(traces))
                .build();
    }

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.doubleValue() <= 0;
    }
}
