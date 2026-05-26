package com.clmcat.qianyu;

import com.clmcat.framework.webmvc.anns.EnableBasicWeb;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBasicWeb
@EnableDubbo
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
