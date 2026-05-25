package com.clmcat.qianyu.social.moment.typehandler;

import com.clmcat.qianyu.core.config.AbstractSqlSessionConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Configuration;


//@ConditionalOnBean(SqlSessionFactory.class)
@Configuration
@Slf4j
public class MomentContentTypeHandlerAutoConfiguration extends AbstractSqlSessionConfiguration {

    @Override
    protected void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(MomentContentTypeHandler.class);
        log.info("Mybatis 类型处理注册: MomentContentTypeHandler");
    }
}