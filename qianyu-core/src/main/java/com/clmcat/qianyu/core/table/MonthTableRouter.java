package com.clmcat.qianyu.core.table;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 按月分表路由工具（UTC 月度）。
 * <p>
 * 分表规则：UTC 时间戳 → 月表后缀。查询时本月 + 上月兜底，覆盖全球时区 ±14h 跨月。
 * <p>
 * 表名格式：{prefix}_{yyyyMM}
 * <ul>
 *   <li>gift_send_record_202608</li>
 *   <li>trade_order_item_202608</li>
 * </ul>
 * 原表名（如 gift_send_record）作为模板表，通过 {@code CREATE TABLE ... LIKE} 生成月表。
 *
 * @author ark-home
 * @date 2026-08-10
 */
public final class MonthTableRouter {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    private MonthTableRouter() {}

    // ==================== 表名解析 ====================

    /** 时间戳 → 月表后缀，如 1754000000000L → "202608" */
    public static String resolveMonth(long utcEpochMs) {
        return YearMonth.from(Instant.ofEpochMilli(utcEpochMs).atZone(ZoneOffset.UTC))
                .format(MONTH_FMT);
    }

    /** 模板表名 + 时间戳 → 月表全名 */
    public static String tableName(String prefix, long utcEpochMs) {
        return prefix + "_" + resolveMonth(utcEpochMs);
    }

    /** 模板表名 + 月份后缀 → 月表全名 */
    public static String tableName(String prefix, String month) {
        return prefix + "_" + month;
    }

    // ==================== 建表相关 ====================

    /** 当前 UTC 月份后缀 */
    public static String currentMonth() {
        return YearMonth.now(ZoneOffset.UTC).format(MONTH_FMT);
    }

    /** 下个月后缀 */
    public static String nextMonth() {
        return YearMonth.now(ZoneOffset.UTC).plusMonths(1).format(MONTH_FMT);
    }

    // ==================== 查询相关 ====================

    /**
     * 查询时需要扫描的月份列表（从 targetMonth 往前数 N 个月）。
     * <p>
     * 典型用法：
     * <ul>
     *   <li>幂等检查：queryMonths(currentMonth(), 2) → [当前月, 上月]</li>
     *   <li>范围查询：queryMonths(endMonth, monthsBetween) → [endMonth, endMonth-1, ...]</li>
     * </ul>
     */
    public static List<String> queryMonths(String latestMonth, int count) {
        List<String> months = new ArrayList<>(count);
        YearMonth ym = YearMonth.parse(latestMonth, MONTH_FMT);
        for (int i = 0; i < count; i++) {
            months.add(ym.format(MONTH_FMT));
            ym = ym.minusMonths(1);
        }
        return months;
    }

    /**
     * 查询时间范围对应的月表列表（含起始月前多 1 个月兜底）。
     *
     * @param fromUtcEpochMs 起始时间戳（含）
     * @param toUtcEpochMs   结束时间戳（含）
     * @return 需扫描的月份列表，从最早到最晚
     */
    public static List<String> queryMonthsRange(long fromUtcEpochMs, long toUtcEpochMs) {
        // 前多 1 个月，覆盖时区偏差
        YearMonth start = YearMonth.from(
                Instant.ofEpochMilli(fromUtcEpochMs).atZone(ZoneOffset.UTC)).minusMonths(1);
        YearMonth end = YearMonth.from(
                Instant.ofEpochMilli(toUtcEpochMs).atZone(ZoneOffset.UTC));

        List<String> months = new ArrayList<>();
        YearMonth cursor = end;
        while (!cursor.isBefore(start)) {
            months.add(0, cursor.format(MONTH_FMT)); // 插在头部保证从早到晚
            cursor = cursor.minusMonths(1);
        }
        return months;
    }

    /** 创建月表的 DDL 语句 */
    public static String createTableSql(String templateTable, String month) {
        String target = tableName(templateTable, month);
        return "CREATE TABLE IF NOT EXISTS " + target + " LIKE " + templateTable;
    }
}
