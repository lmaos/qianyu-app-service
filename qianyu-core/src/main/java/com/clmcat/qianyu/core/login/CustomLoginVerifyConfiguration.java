package com.clmcat.qianyu.core.login;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomLoginVerifyConfiguration {

    @Bean
    CustomLoginVerifyFunction defaultLoginVerifyFunction() {
        return new CustomLoginVerifyFunction();
    }

}
