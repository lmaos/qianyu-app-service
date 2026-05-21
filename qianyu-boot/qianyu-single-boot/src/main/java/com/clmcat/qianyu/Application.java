package com.clmcat.qianyu;

import com.clmcat.framework.webmvc.anns.EnableBasicWeb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBasicWeb
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
