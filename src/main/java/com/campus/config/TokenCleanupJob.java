package com.campus.config;

import com.campus.user.mapper.TokenMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupJob.class);

    private final TokenMapper tokenMapper;

    public TokenCleanupJob(TokenMapper tokenMapper) {
        this.tokenMapper = tokenMapper;
    }

    /** Purge expired tokens hourly. */
    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpiredTokens() {
        int removed = tokenMapper.deleteExpired(LocalDateTime.now());
        if (removed > 0) {
            log.info("Purged {} expired tokens", removed);
        }
    }
}
