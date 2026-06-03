package com.clmcat.qianyu.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({org.apache.ibatis.session.SqlSessionFactory.class})
@Slf4j
public class DefaultSqlSessionAutoConfiguration extends AbstractSqlSessionConfiguration {

    @Override
    protected void configuration(org.apache.ibatis.session.Configuration configuration) {
        log.info("DefaultSqlSessionAutoConfiguration: MyBatis Configuration - mapUnderscoreToCamelCase set to true");
        configuration.setMapUnderscoreToCamelCase(true);
    }
}
