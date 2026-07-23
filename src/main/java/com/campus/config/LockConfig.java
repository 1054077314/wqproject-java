package com.campus.config;

import com.campus.lock.DistributedLock;
import com.campus.lock.NoOpDistributedLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!redis")
public class LockConfig {

    @Bean
    public DistributedLock noOpDistributedLock() {
        return new NoOpDistributedLock();
    }
}
