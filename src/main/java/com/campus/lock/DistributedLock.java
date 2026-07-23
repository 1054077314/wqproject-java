package com.campus.lock;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Cross-instance mutual exclusion. Default impl is no-op (single-instance + DB CAS is enough);
 * Redis profile provides a real SET NX lock for multi-instance deploys.
 */
public interface DistributedLock {

    <T> T execute(String key, Duration ttl, Supplier<T> action);
}
