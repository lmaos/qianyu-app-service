package com.clmcat.qianyu.core.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁上下文。
 * <p>
 * 单个锁对象可持有多个 key，适合配合 try-with-resources 使用，
 * 在 close() 时自动释放当前对象成功持有的全部 key。
 */
public class RedisLock implements AutoCloseable {

    private static final long DEFAULT_WAIT_MILLIS = 1000L;
    private static final long DEFAULT_RETRY_INTERVAL_MILLIS = 50L;
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end",
            Long.class
    );

    private final RedisTemplate<String, String> redisTemplate;
    private final long maxHoldSeconds;
    private final Map<String, String> lockedKeyTokenMap = new LinkedHashMap<>();
    private boolean closed = false;

    RedisLock(RedisTemplate<String, String> redisTemplate, long maxHoldSeconds) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate must not be null");
        if (maxHoldSeconds <= 0) {
            throw new IllegalArgumentException("maxHoldSeconds must be greater than 0");
        }
        this.maxHoldSeconds = maxHoldSeconds;
    }

    /**
     * 按默认等待时间尝试获取一个 key 的锁。
     *
     * @param lockKey 锁 key
     * @return true 表示成功持有该 key
     */
    public boolean lock(String lockKey) {
        return lock(lockKey, DEFAULT_WAIT_MILLIS);
    }

    /**
     * 在指定等待时间内尝试获取一个 key 的锁。
     *
     * @param lockKey 锁 key
     * @param waitMillis 获取锁的最长等待毫秒数
     * @return true 表示成功持有该 key
     */
    public boolean lock(String lockKey, long waitMillis) {
        assertUsable();
        validateLockKey(lockKey);
        if (waitMillis < 0) {
            throw new IllegalArgumentException("waitMillis must not be less than 0");
        }
        if (lockedKeyTokenMap.containsKey(lockKey)) {
            return true;
        }

        String lockValue = UUID.randomUUID().toString();
        long deadline = System.currentTimeMillis() + waitMillis;
        do {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, maxHoldSeconds, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                lockedKeyTokenMap.put(lockKey, lockValue);
                return true;
            }
            if (System.currentTimeMillis() >= deadline) {
                return false;
            }
            try {
                Thread.sleep(DEFAULT_RETRY_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        } while (true);
    }

    /**
     * tryLock 语义别名，便于调用方按自己习惯使用。
     *
     * @param lockKey 锁 key
     * @return true 表示成功持有该 key
     */
    public boolean tryLock(String lockKey) {
        return lock(lockKey);
    }

    /**
     * tryLock 语义别名，便于调用方按自己习惯使用。
     *
     * @param lockKey 锁 key
     * @param waitMillis 获取锁的最长等待毫秒数
     * @return true 表示成功持有该 key
     */
    public boolean tryLock(String lockKey, long waitMillis) {
        return lock(lockKey, waitMillis);
    }

    /**
     * 判断当前锁上下文是否已经持有指定 key。
     *
     * @param lockKey 锁 key
     * @return true 表示当前对象已持有该 key
     */
    public boolean hasLock(String lockKey) {
        return lockedKeyTokenMap.containsKey(lockKey);
    }

    /**
     * 释放当前锁上下文成功持有的全部 key。
     * <p>
     * 每个 key 都会按自己的 token 做 compare-and-delete，避免误删其他请求后来重新获取到的锁。
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        List<Map.Entry<String, String>> entries = new ArrayList<>(lockedKeyTokenMap.entrySet());
        for (int i = entries.size() - 1; i >= 0; i--) {
            Map.Entry<String, String> entry = entries.get(i);
            redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(entry.getKey()), entry.getValue());
        }
        lockedKeyTokenMap.clear();
        closed = true;
    }

    private void assertUsable() {
        if (closed) {
            throw new IllegalStateException("RedisLock has been closed");
        }
    }

    private void validateLockKey(String lockKey) {
        if (lockKey == null || lockKey.isBlank()) {
            throw new IllegalArgumentException("lockKey must not be blank");
        }
    }
}
