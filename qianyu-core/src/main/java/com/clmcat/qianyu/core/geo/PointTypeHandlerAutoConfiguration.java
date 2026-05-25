package com.clmcat.qianyu.core.geo;

import com.clmcat.qianyu.core.config.AbstractSqlSessionConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({SqlSessionFactory.class, org.locationtech.jts.geom.Point.class})
@Slf4j
public class PointTypeHandlerAutoConfiguration extends AbstractSqlSessionConfiguration {

    @Override
    protected void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(org.locationtech.jts.geom.Point.class, new PointTypeHandler());
        log.info("Mybatis 类型处理注册: PointTypeHandler");
    }

}