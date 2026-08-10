package com.clmcat.qianyu.payment.trade.scheduler;

import com.clmcat.qianyu.core.table.MonthTableRouter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

/**
 * 月表自动创建器（payment 模块）。
 * <p>
 * 为 trade_order_item 按月建表。启动时创建上月 + 当月 + 下月，每月 26/27/28 日三次兜底。
 *
 * @author ark-home
 * @date 2026-08-10
 */
@Component("paymentMonthTableInitializer")
public class MonthTableInitializer {

    private static final Logger log = LoggerFactory.getLogger(MonthTableInitializer.class);

    private static final List<String> TEMPLATE_TABLES = java.util.Arrays.asList(
            "trade_order_item",
            "transaction_record",
            "settlement_record"
    );

    private final JdbcTemplate jdbcTemplate;

    public MonthTableInitializer(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void ensureTablesOnStartup() {
        String current = MonthTableRouter.currentMonth();
        String next = MonthTableRouter.nextMonth();
        String last = MonthTableRouter.queryMonths(current, 2).get(1);

        for (String template : TEMPLATE_TABLES) {
            createIfNotExists(template, last);
            createIfNotExists(template, current);
            createIfNotExists(template, next);
        }

        log.info("月表检查完成: last={}, current={}, next={}, templates={}", last, current, next, TEMPLATE_TABLES);
    }

    @Scheduled(cron = "0 0 3 26,27,28 * ?")
    public void ensureNextMonthTable() {
        String next = MonthTableRouter.nextMonth();
        for (String template : TEMPLATE_TABLES) {
            createIfNotExists(template, next);
        }
        log.info("定时建表检查完成: nextMonth={}", next);
    }

    private void createIfNotExists(String template, String month) {
        String sql = MonthTableRouter.createTableSql(template, month);
        try {
            jdbcTemplate.execute(sql);
            log.debug("月表已就绪: {}", MonthTableRouter.tableName(template, month));
        } catch (Exception e) {
            log.error("创建月表失败: sql={}", sql, e);
        }
    }
}
