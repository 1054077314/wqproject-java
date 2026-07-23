package com.campus.lock;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Test/helper lock: serialize by key (same semantics as Redis SET NX for a single JVM).
 */
public class SynchronizedDistributedLock implements DistributedLock {

    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    public <T> T execute(String key, Duration ttl, Supplier<T> action) {
        Object monitor = locks.computeIfAbsent(key, k -> new Object());
        synchronized (monitor) {
            return action.get();
        }
    }
}
