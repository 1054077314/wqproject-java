package com.campus.lock;

import com.campus.common.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis SET NX EX lock. Token-based unlock avoids deleting another holder's key.
 */
public class RedisDistributedLock implements DistributedLock {

    private final StringRedisTemplate redis;

    public RedisDistributedLock(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public <T> T execute(String key, Duration ttl, Supplier<T> action) {
        String lockKey = "lock:" + key;
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(lockKey, token, ttl.toMillis(), TimeUnit.MILLISECONDS);
        if (!Boolean.TRUE.equals(ok)) {
            throw new BusinessException(409, "操作冲突，请稍后重试");
        }
        try {
            return action.get();
        } finally {
            String current = redis.opsForValue().get(lockKey);
            if (token.equals(current)) {
                redis.delete(lockKey);
            }
        }
    }
}
