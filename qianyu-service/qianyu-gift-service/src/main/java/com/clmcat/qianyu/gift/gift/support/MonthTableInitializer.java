package com.clmcat.qianyu.gift.gift.support;

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
 * 月表自动创建器。
 * <p>
 * 启动时检查本月 + 下月表是否存在，不存在则 CREATE TABLE ... LIKE 模板表。
 * 每月 26/27/28 日凌晨 3 点再次检查下月表，三次兜底防止遗漏。
 *
 * @author ark-home
 * @date 2026-08-10
 */
@Component("giftMonthTableInitializer")
public class MonthTableInitializer {

    private static final Logger log = LoggerFactory.getLogger(MonthTableInitializer.class);

    /** 需要按月分表的模板表名列表 */
    private static final List<String> TEMPLATE_TABLES = Collections.singletonList("gift_send_record");

    private final JdbcTemplate jdbcTemplate;

    public MonthTableInitializer(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void ensureTablesOnStartup() {
        String current = MonthTableRouter.currentMonth();
        String next = MonthTableRouter.nextMonth();
        String last = MonthTableRouter.queryMonths(current, 2).get(1); // 上月

        for (String template : TEMPLATE_TABLES) {
            createIfNotExists(template, last);     // 上月：查询兜底
            createIfNotExists(template, current);  // 本月：写入目标
            createIfNotExists(template, next);     // 下月：提前建
        }

        log.info("月表检查完成: last={}, current={}, next={}, templates={}", last, current, next, TEMPLATE_TABLES);
    }

    /**
     * 每月 26/27/28 日 03:00:00 执行。
     * 三次冗余调度，保证下月表在月底前必然创建。
     */
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
