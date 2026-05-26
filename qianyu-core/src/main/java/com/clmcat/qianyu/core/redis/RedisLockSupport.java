package com.clmcat.qianyu.core.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

/**
 * Redis 分布式锁工具。
 */
public class RedisLockSupport {

    /**
     * 创建一个 Redis 锁上下文。
     *
     * @param redisTemplate RedisTemplate
     * @param maxHoldSeconds 锁最长持有秒数
     * @return Redis 锁上下文对象
     */
    public static RedisLock newLock(RedisTemplate<String, String> redisTemplate, long maxHoldSeconds) {
        Objects.requireNonNull(redisTemplate, "redisTemplate must not be null");
        return new RedisLock(redisTemplate, maxHoldSeconds);
    }

    /**
     * newLock 的小写别名，便于按调用习惯使用。
     *
     * @param redisTemplate RedisTemplate
     * @param maxHoldSeconds 锁最长持有秒数
     * @return Redis 锁上下文对象
     */
    public static RedisLock newlock(RedisTemplate<String, String> redisTemplate, long maxHoldSeconds) {
        return newLock(redisTemplate, maxHoldSeconds);
    }
}
