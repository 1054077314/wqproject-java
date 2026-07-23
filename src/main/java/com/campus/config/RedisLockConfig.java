package com.campus.config;

import com.campus.lock.DistributedLock;
import com.campus.lock.RedisDistributedLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Profile("redis")
public class RedisLockConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${app.redis.host:127.0.0.1}") String host,
            @Value("${app.redis.port:6379}") int port) {
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(conf);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public DistributedLock distributedLock(StringRedisTemplate stringRedisTemplate) {
        return new RedisDistributedLock(stringRedisTemplate);
    }
}
