package com.clmcat.qianyu.social.comment.typehandler;

import com.clmcat.qianyu.core.config.AbstractSqlSessionConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CommentContentTypeHandlerAutoConfiguration extends AbstractSqlSessionConfiguration {
    @Override
    protected void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(CommentContentTypeHandler.class);
        log.info("Mybatis 类型处理注册: CommentContentTypeHandler");
    }
}
