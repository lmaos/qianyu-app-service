package com.clmcat.qianyu.core.config;

import lombok.Setter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AbstractSqlSessionConfiguration implements SmartInitializingSingleton , ApplicationContextAware {


    @Setter
    protected ApplicationContext applicationContext;

    @Override
    public void afterSingletonsInstantiated() {
        applicationContext.getBeansOfType(SqlSessionFactory.class).values().forEach(sqlSessionFactory -> {
            configSqlSessionFactoryBean(sqlSessionFactory);
            registerTypeHandler(sqlSessionFactory.getConfiguration().getTypeHandlerRegistry());
        });
    }


    protected void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {

    }

    protected void configSqlSessionFactoryBean(SqlSessionFactory sqlSessionFactory) {

    }
}
