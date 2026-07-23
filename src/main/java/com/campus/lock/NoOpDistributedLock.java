package com.campus.lock;

import java.time.Duration;
import java.util.function.Supplier;

public class NoOpDistributedLock implements DistributedLock {

    @Override
    public <T> T execute(String key, Duration ttl, Supplier<T> action) {
        return action.get();
    }
}
