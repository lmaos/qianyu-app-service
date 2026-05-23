package com.clmcat.qianyu.core.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.Socket;

@Configuration
@Profile("dev")
@ConditionalOnClass({
    RedisConnectionFactory.class,  // 有Redis依赖才生效
    RedisServer.class               // 有embedded-redis才生效
})
@ConditionalOnProperty(prefix = "spring.data.redis", name = "host", havingValue = "false", matchIfMissing = true)
public class DevRedisConfiguration {

    private RedisServer redisServer;


    private static final int REDIS_PORT = 6379;

    private int redisPort;
    // 启动 Redis（随机端口）
    @PostConstruct
    public void startRedis() throws IOException {
        if (isPortInUse("localhost", REDIS_PORT)) {
            System.out.println("⚠️ 端口 " + REDIS_PORT + " 已启动，跳过内嵌Redis启动");
            redisPort = REDIS_PORT;
            return;
        }
        // 🔥 重点：随机端口！
        redisServer = RedisServer.builder()
                .port(16379) // 0 = 系统自动分配空闲端口
                .build();
        redisServer.start();
        redisPort = redisServer.ports().get(0);
        System.out.println("✅ 内嵌Redis启动成功，随机端口：" + redisPort);
    }

    // 停止 Redis
    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    // 🔥 自动创建连接工厂（使用随机端口）
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", redisPort);
    }

    // 判断 6379 是否开启
    private boolean isPortInUse(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
